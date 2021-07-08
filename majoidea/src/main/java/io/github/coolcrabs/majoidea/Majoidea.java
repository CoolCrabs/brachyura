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

import java.io.Closeable;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.FileSystem;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.Problem;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.visitor.GenericVisitor;
import com.github.javaparser.ast.visitor.Visitable;
import com.github.javaparser.printer.lexicalpreservation.LexicalPreservingPrinter;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.model.resolution.TypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;

import net.fabricmc.mappingio.tree.MappingTree;

public final class Majoidea implements Closeable {
    final static Logger logger = Logger.getLogger("majoidea");

    final JavaParser javaParser;
    final List<Path> files;
    final Path outDir;
    final List<FileSystem> toClose;
    final List<GenericVisitor<Visitable, ?>> visitors = new ArrayList<>();

    public Majoidea(Path srcDir, Path outDir, List<Path> srcClasspathJars) throws IOException {
        this.outDir = outDir;
        this.toClose = new ArrayList<>(srcClasspathJars.size());
        ArrayList<TypeSolver> typeSolvers = new ArrayList<>();
        try {
            typeSolvers.add(new JavaParserTypeSolver(srcDir));
            for (Path path : srcClasspathJars) {
                FileSystem fileSystem = FileSystemUtil.newJarFileSystem(path);
                toClose.add(fileSystem);
                typeSolvers.add(new ClassDirTypeSolver(fileSystem.getPath("/")));
            }
        } catch (Exception e) {
            for (FileSystem fileSystem : toClose) {
                fileSystem.close();
            }
            throw e;
        }
        typeSolvers.add(new ReflectionTypeSolver(true));
        TypeSolver typeSolver = new CombinedTypeSolver(typeSolvers);
        JavaSymbolSolver javaSymbolSolver = new JavaSymbolSolver(typeSolver);
        ParserConfiguration parserConfiguration = new ParserConfiguration();
        parserConfiguration.setSymbolResolver(javaSymbolSolver);
        parserConfiguration.setLexicalPreservationEnabled(true);
        files = new ArrayList<>();
        Files.walkFileTree(srcDir, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                if (file.toString().endsWith(".java")) files.add(file);
                return FileVisitResult.CONTINUE;
            }
        });
        javaParser = new JavaParser(parserConfiguration);
    }

    public void remap(MappingTree mappings, int srcNamespace, int dstNamespace) {
        visitors.add(new RemapModifier(mappings, srcNamespace, dstNamespace));
    }

    public void customVisitor(GenericVisitor<Visitable, ?> visitor) {
        visitors.add(visitor);
    }

    public void run() throws IOException {
        for (Path file : files) {
            ParseResult<CompilationUnit> parseResult = javaParser.parse(file);
            try {
                Optional<CompilationUnit> ocu = parseResult.getResult();
                if (parseResult.isSuccessful() || !ocu.isPresent()) {
                    CompilationUnit cu = ocu.get();
                    for (GenericVisitor<Visitable, ?> visitor : visitors) {
                        // cu = (CompilationUnit) visitor.visit(cu, null);
                        visitor.visit(cu, null);
                    }
                    LexicalPreservingPrinter.setup(cu);
                    logger.info(cu.toString());
                    logger.info(LexicalPreservingPrinter.print(cu));
                } else {
                    logger.warning("Error parsing " + file);
                    for (Problem problem : parseResult.getProblems()) {
                        logger.warning(problem.getMessage());
                    }
                }
            } catch (Exception e) {
                logger.warning("Error parsing " + file);
                StringWriter stringWriter = new StringWriter();
                PrintWriter printWriter = new PrintWriter(stringWriter);
                e.printStackTrace(printWriter);
                logger.warning(stringWriter.toString());
            }
        }
    }

    @Override
    public void close() throws IOException {
        for (FileSystem fileSystem : toClose) {
            fileSystem.close();
        }
    }
}
