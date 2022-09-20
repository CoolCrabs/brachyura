package io.github.coolcrabs.brachyura.compiler.java;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.TreeMap;
import java.util.Map.Entry;

import javax.tools.JavaFileObject;
import javax.tools.JavaFileObject.Kind;

import io.github.coolcrabs.brachyura.processing.ProcessingSource;

class InputFiles {
    TreeMap<String, InputFile> files = new TreeMap<>();

    public void add(ProcessingSource s) {
        s.getInputs((in, id) -> {
            files.put(id.path, new InputFile(in, id));
        });
    }

    Iterator<JavaFileObject> it(String packageName, Set<Kind> kinds, boolean recurse) {
        String slashedPkg = packageName.replace('.', '/');
        return new Iterator<JavaFileObject>() {
            Iterator<Entry<String, InputFile>> c = files.tailMap(slashedPkg).entrySet().iterator();
            InputFile next = advance();

            private InputFile advance() {
                while (c.hasNext()) {
                    Entry<String, InputFile> e = c.next();
                    if (!e.getKey().startsWith(slashedPkg)) {
                        return null; // We've gone past this package
                    }
                    if (!recurse && e.getKey().lastIndexOf('/') > packageName.length()) {
                        continue; // Subpackage
                    }
                    if (kinds.contains(e.getValue().getKind())) {
                        return e.getValue();
                    }
                }
                return null;
            }

            public boolean hasNext() {
                return next != null;
            }

            public JavaFileObject next() {
                if (!hasNext()) throw new NoSuchElementException();
                InputFile r = next;
                next = advance();
                return r;
            }
        };
    }
}
