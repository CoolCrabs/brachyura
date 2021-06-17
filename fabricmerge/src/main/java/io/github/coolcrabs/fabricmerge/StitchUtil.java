/*
 * Copyright (c) 2016, 2017, 2018, 2019 FabricMC
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

package io.github.coolcrabs.fabricmerge;

import org.objectweb.asm.Opcodes;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystemAlreadyExistsException;
import java.nio.file.FileSystems;
import java.util.*;

final class StitchUtil {

    public static int ASM_VERSION = Opcodes.ASM9;

    public static class FileSystemDelegate implements AutoCloseable {
        private final FileSystem fileSystem;
        private final boolean owner;

        public FileSystemDelegate(FileSystem fileSystem, boolean owner) {
            this.fileSystem = fileSystem;
            this.owner = owner;
        }

        public FileSystem get() {
            return fileSystem;
        }

        @Override
        public void close() throws IOException {
            if (owner) {
                fileSystem.close();
            }
        }
    }

    private StitchUtil() {

    }

    private static final Map<String, String> jfsArgsCreate = new HashMap<>();
    private static final Map<String, String> jfsArgsEmpty = new HashMap<>();

    static {
        jfsArgsCreate.put("create", "true");
    }

    public static FileSystemDelegate getJarFileSystem(File f, boolean create) throws IOException {
        URI jarUri;
        try {
            jarUri = new URI("jar:file", null, f.toURI().getPath(), "");
        } catch (URISyntaxException e) {
            throw new IOException(e);
        }

        try {
            return new FileSystemDelegate(FileSystems.newFileSystem(jarUri, create ? jfsArgsCreate : jfsArgsEmpty), true);
        } catch (FileSystemAlreadyExistsException e) {
            return new FileSystemDelegate(FileSystems.getFileSystem(jarUri), false);
        }
    }

    public static String join(String joiner, Collection<String> c) {
        StringBuilder builder = new StringBuilder();
        int i = 0;
        for (String s : c) {
            if ((i++) > 0) {
                builder.append(joiner);
            }

            builder.append(s);
        }
        return builder.toString();
    }

    public static <T> Set<T> newIdentityHashSet() {
        return Collections.newSetFromMap(new IdentityHashMap<>());
    }

    public static List<String> mergePreserveOrder(List<String> first, List<String> second) {
        List<String> out = new ArrayList<>();
        int i = 0;
        int j = 0;

        while (i < first.size() || j < second.size()) {
            while (i < first.size() && j < second.size()
                    && first.get(i).equals(second.get(j))) {
                out.add(first.get(i));
                i++;
                j++;
            }

            while (i < first.size() && !second.contains(first.get(i))) {
                out.add(first.get(i));
                i++;
            }

            while (j < second.size() && !first.contains(second.get(j))) {
                out.add(second.get(j));
                j++;
            }
        }

        return out;
    }

    public static long getTime() {
        return new Date().getTime();
    }
}