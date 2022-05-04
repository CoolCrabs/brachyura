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

import java.io.IOException;
import java.io.UncheckedIOException;
import java.io.Writer;

public final class AccessWidenerWriter implements AccessWidenerVisitor {
    private final Writer w;
    private int version;
    private String namespace;

    /**
     * Constructs a writer that writes an access widener in the given version.
     * If features not supported by the version are used, an exception is thrown.
     */
    public AccessWidenerWriter(Writer w, int version) {
        this.version = version;
        this.w = w;
    }

    public AccessWidenerWriter(Writer w) {
        this.version = -1;
        this.w = w;
    }

    @Override
    public void visitVersion(int version) {
        if (this.version == -1) this.version = version;
    }

    @Override
    public void visitHeader(String namespace) {
        if (version < 0 || version > 2) throw new IllegalArgumentException("Invalid version " + version);
        try {
            if (this.namespace == null) {
                w.append("accessWidener\tv");
                        w.append(Integer.toString(version));
                        w.append('\t');
                        w.append(namespace);
                        w.append('\n');
            } else if (!this.namespace.equals(namespace)) {
                throw new IllegalArgumentException("Cannot write different namespaces to the same file ("
                        + this.namespace + " != " + namespace + ")");
            }

            this.namespace = namespace;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } 
    }

    @Override
    public void visitClass(String name, AccessWidenerReader.AccessType access, boolean transitive) {
        writeAccess(access, transitive);
        try {
            w.append("\tclass\t").append(name).append('\n');
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void visitMethod(String owner, String name, String descriptor, AccessWidenerReader.AccessType access, boolean transitive) {
        writeAccess(access, transitive);
        try {
            w.append("\tmethod\t").append(owner).append('\t').append(name)
                .append('\t').append(descriptor).append('\n');
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void visitField(String owner, String name, String descriptor, AccessWidenerReader.AccessType access, boolean transitive) {
        writeAccess(access, transitive);
        try {
            w.append("\tfield\t").append(owner).append('\t').append(name)
                .append('\t').append(descriptor).append('\n');
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private void writeAccess(AccessWidenerReader.AccessType access, boolean transitive) {
        try {
            if (transitive) {
                if (version < 2) {
                    throw new IllegalStateException("Cannot write transitive rule in version " + version);
                }

                w.append("transitive-");
            }

            w.append(access.toString());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
