package io.github.coolcrabs.javacompilelib;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

public final class ForkingJavaCompilation implements JavaCompilation {
    static final File thisjar;

    final String java;

    static {
        try {
            // https://stackoverflow.com/a/2837287
            thisjar = new File(ForkingJavaCompilation.class.getProtectionDomain().getCodeSource().getLocation().toURI());
        } catch (Exception e) {
            Util.doThrow(e);
            throw null;
        }
    }

    public ForkingJavaCompilation(String java) {
        this.java = java;
    }

    static class StreamRedirector extends Thread {
        final InputStream is;
        final OutputStream os;

        StreamRedirector(InputStream is, OutputStream os) {
            this.is = is;
            this.os = os;
        }

        @Override
        public void run() {
            try {
                byte[] buf = new byte[8192];
                int length;
                while ((length = is.read(buf)) > 0) {
                    os.write(buf, 0, length);
                }
            } catch (Exception e) {
                Util.doThrow(e);
                throw null;
            }
        }
    }

    @Override
    public boolean compile(JavaCompilationUnit javaCompilationUnit) {
        try {
            Process process = new ProcessBuilder(java.toString(), "-cp", thisjar.toString(), "io.github.coolcrabs.javacompilelib.ForkingJavaCompilationEntry").redirectErrorStream(true).start();
            // Java 7 added some nice methods for this, why am I using Java 6 again?
            StreamRedirector streamRedirector = new StreamRedirector(new BufferedInputStream(process.getInputStream()), System.out);
            streamRedirector.start();
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(new BufferedOutputStream(process.getOutputStream()));
            objectOutputStream.writeObject(javaCompilationUnit);
            objectOutputStream.close();
            return process.waitFor() == 0;
        } catch (Exception e) {
            Util.doThrow(e);
            throw null;
        }
    }
}
