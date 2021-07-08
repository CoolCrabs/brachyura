/*
 * Copyright (C) 2015-2016 Federico Tomassetti
 * Copyright (C) 2017-2020 The JavaParser Team.
 * Copyright (C) 2021 CoolCrabs.
 *
 * This file is part of majoidea.
 *
 * Majoidea can be used either under the terms of
 * a) the GNU Lesser General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 * b) the terms of the Apache License
 *
 * You should have received a copy of both licenses in LICENCE.LGPL and
 * LICENCE.APACHE. Please refer to those files for details.
 *
 * JavaParser is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 */

package io.github.coolcrabs.majoidea;

import com.github.javaparser.resolution.UnsolvedSymbolException;
import com.github.javaparser.resolution.declarations.ResolvedReferenceTypeDeclaration;
import com.github.javaparser.symbolsolver.javassistmodel.JavassistFactory;
import com.github.javaparser.symbolsolver.model.resolution.SymbolReference;
import com.github.javaparser.symbolsolver.model.resolution.TypeSolver;

import javassist.ClassPool;
import javassist.NotFoundException;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

// Based on JarTypeSolver

class ClassDirTypeSolver implements TypeSolver {
    
    private static final String CLASS_EXTENSION = ".class";

    /**
     * Convert the entry path into a qualified name.
     *
     * The entries in Jar files follows the format {@code com/github/javaparser/ASTParser$JJCalls.class}
     * while in the type solver we need to work with {@code com.github.javaparser.ASTParser.JJCalls}.
     *
     * @param entryPath The entryPath to be converted.
     *
     * @return The qualified name for the entryPath.
     */
    private static String convertEntryPathToClassName(String entryPath) {
        if (!entryPath.endsWith(CLASS_EXTENSION)) {
            throw new IllegalArgumentException(String.format("The entry path should end with %s", CLASS_EXTENSION));
        }
        String className = entryPath.substring(0, entryPath.length() - CLASS_EXTENSION.length());
        className = className.replace('/', '.');
        className = className.replace('$', '.');
        return className;
    }

    /**
     * Convert the entry path into a qualified name to be used in {@link ClassPool}.
     *
     * The entries in Jar files follows the format {@code com/github/javaparser/ASTParser$JJCalls.class}
     * while in the class pool we need to work with {@code com.github.javaparser.ASTParser$JJCalls}.
     *
     * @param entryPath The entryPath to be converted.
     *
     * @return The qualified name to be used in the class pool.
     */
    private static String convertEntryPathToClassPoolName(String entryPath) {
        if (!entryPath.endsWith(CLASS_EXTENSION)) {
            throw new IllegalArgumentException(String.format("The entry path should end with %s", CLASS_EXTENSION));
        }
        String className = entryPath.substring(0, entryPath.length() - CLASS_EXTENSION.length());
        return className.replace('/', '.');
    }

    private final ClassPool classPool = new ClassPool();
    private final Map<String, String> knownClasses = new HashMap<>();

    private TypeSolver parent;

    /**
     * Create a {@link JarTypeSolver} from a {@link Path}.
     *
     * @param pathToJar The path where the jar is located.
     *
     * @throws IOException If an I/O exception occurs while reading the Jar.
     */
    public ClassDirTypeSolver(Path dir) throws IOException {
        classPool.appendClassPath(new DirPathClassPath(dir));
        registerKnownClassesFor(dir);
    }

    /**
     * Register the list of known classes.
     *
     * When we create a new {@link JarTypeSolver} we should store the list of
     * solvable types.
     *
     * @param pathToJar The path to the jar file.
     *
     * @throws IOException If an I/O error occurs while reading the JarFile.
     */
    private void registerKnownClassesFor(Path dir) throws IOException {
        Files.walkFileTree(dir, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                String fileName = dir.relativize(file).toString().replace(file.getFileSystem().getSeparator(), "/");
                if (fileName.endsWith(CLASS_EXTENSION)) {
                    String qualifiedName = convertEntryPathToClassName(fileName);
                    String classPoolName = convertEntryPathToClassPoolName(fileName);

                    // If the qualified name is the same as the class pool name we don't need to duplicate store two
                    // different String instances. Let's reuse the same.
                    if (qualifiedName.equals(classPoolName)) {
                        knownClasses.put(qualifiedName, qualifiedName);
                    } else {
                        knownClasses.put(qualifiedName, classPoolName);
                    }
                }
                return FileVisitResult.CONTINUE;
            }
        });
    }

    /**
     * Get the set of classes that can be resolved in the current type solver.
     *
     * @return The set of known classes.
     */
    public Set<String> getKnownClasses() {
        return knownClasses.keySet();
    }

    @Override
    public TypeSolver getParent() {
        return parent;
    }

    @Override
    public void setParent(TypeSolver parent) {
        Objects.requireNonNull(parent);
        if (this.parent != null) {
            throw new IllegalStateException("This TypeSolver already has a parent.");
        }
        if (parent == this) {
            throw new IllegalStateException("The parent of this TypeSolver cannot be itself.");
        }
        this.parent = parent;
    }

    @Override
    public SymbolReference<ResolvedReferenceTypeDeclaration> tryToSolveType(String name) {

        String storedKey = knownClasses.get(name);
        // If the name is not registered in the list we can safely say is not solvable here
        if (storedKey == null) {
            return SymbolReference.unsolved(ResolvedReferenceTypeDeclaration.class);
        }

        try {
            return SymbolReference.solved(JavassistFactory.toTypeDeclaration(classPool.get(storedKey), getRoot()));
        } catch (NotFoundException e) {
            // The names in stored key should always be resolved.
            // But if for some reason this happen, the user is notified.
            throw new IllegalStateException(String.format(
                    "Unable to get class with name %s from class pool." +
                    "This was not suppose to happen, please report at https://github.com/javaparser/javaparser/issues",
                    storedKey));
        }
    }

    @Override
    public ResolvedReferenceTypeDeclaration solveType(String name) throws UnsolvedSymbolException {
        SymbolReference<ResolvedReferenceTypeDeclaration> ref = tryToSolveType(name);
        if (ref.isSolved()) {
            return ref.getCorrespondingDeclaration();
        } else {
            throw new UnsolvedSymbolException(name);
        }
    }

}
