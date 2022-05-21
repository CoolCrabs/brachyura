/*
 * Copyright (c) 2020 FabricMC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.coolcrabs.accesswidener;

/**
 * A visitor of the entries defined in an access widener file.
 */
public interface AccessWidenerVisitor {
    /**
     * Visits the version.
     *
     * @param version the access widener's version
     */
    default void visitVersion(int version) {
    }

    /**
     * Visits the header data.
     *
     * @param namespace the access widener's mapping namespace
     */
    default void visitHeader(String namespace) {
    }

    /**
     * Visits a widened class.
     *
     * @param name       the name of the class
     * @param access     the access type ({@link AccessWidenerReader.AccessType#ACCESSIBLE} or {@link AccessWidenerReader.AccessType#EXTENDABLE})
     * @param transitive whether this widener should be applied across mod boundaries
     */
    default void visitClass(String name, AccessWidenerReader.AccessType access, boolean transitive) {
    }

    /**
     * Visits a widened method.
     *
     * @param owner      the name of the containing class
     * @param name       the name of the method
     * @param descriptor the method descriptor
     * @param access     the access type ({@link AccessWidenerReader.AccessType#ACCESSIBLE} or {@link AccessWidenerReader.AccessType#EXTENDABLE})
     * @param transitive whether this widener should be applied across mod boundaries
     */
    default void visitMethod(String owner, String name, String descriptor, AccessWidenerReader.AccessType access, boolean transitive) {
    }

    /**
     * Visits a widened field.
     *
     * @param owner      the name of the containing class
     * @param name       the name of the field
     * @param descriptor the type of the field as a type descriptor
     * @param access     the access type ({@link AccessWidenerReader.AccessType#ACCESSIBLE} or {@link AccessWidenerReader.AccessType#MUTABLE})
     * @param transitive whether this widener should be applied across mod boundaries
     */
    default void visitField(String owner, String name, String descriptor, AccessWidenerReader.AccessType access, boolean transitive) {
    }
}
