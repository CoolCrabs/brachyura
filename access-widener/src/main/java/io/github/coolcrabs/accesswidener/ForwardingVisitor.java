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
 * Forwards visitor events to multiple other visitors.
 */
public class ForwardingVisitor implements AccessWidenerVisitor {
    private final AccessWidenerVisitor[] visitors;

    public ForwardingVisitor(AccessWidenerVisitor... visitors) {
        this.visitors = visitors.clone();
    }

    @Override
    public void visitHeader(String namespace) {
        for (AccessWidenerVisitor visitor : visitors) {
            visitor.visitHeader(namespace);
        }
    }

    @Override
    public void visitClass(String name, AccessWidenerReader.AccessType access, boolean transitive) {
        for (AccessWidenerVisitor visitor : visitors) {
            visitor.visitClass(name, access, transitive);
        }
    }

    @Override
    public void visitMethod(String owner, String name, String descriptor, AccessWidenerReader.AccessType access, boolean transitive) {
        for (AccessWidenerVisitor visitor : visitors) {
            visitor.visitMethod(owner, name, descriptor, access, transitive);
        }
    }

    @Override
    public void visitField(String owner, String name, String descriptor, AccessWidenerReader.AccessType access, boolean transitive) {
        for (AccessWidenerVisitor visitor : visitors) {
            visitor.visitField(owner, name, descriptor, access, transitive);
        }
    }
}
