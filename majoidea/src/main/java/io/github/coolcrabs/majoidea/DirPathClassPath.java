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

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

import javassist.ClassPath;
import javassist.NotFoundException;

class DirPathClassPath implements ClassPath {
    final Path dir;

    public DirPathClassPath(Path dir) {
        this.dir = dir;
    }

    Path getPath(String className) {
        return dir.resolve(className.replace('.', '/') + ".class");
    }

    @Override
    public InputStream openClassfile(String className) throws NotFoundException {
        try {
            return Files.newInputStream(getPath(className));
        } catch (IOException e) {
            throw new NotFoundException(e + "\n" + e.getMessage());
        }
    }

    @Override
    public URL find(String className) {
        try {
            return getPath(className).toUri().toURL();
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return null;
        }
    }
    
}
