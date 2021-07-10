package io.github.coolcrabs.javacompilelib;

import java.io.ObjectInputStream;

class ForkingJavaCompilationEntry {
    public static void main(String[] args) throws Exception {
        ObjectInputStream objectInputStream = new ObjectInputStream(System.in);
        JavaCompilationUnit javaCompilationUnit = (JavaCompilationUnit) objectInputStream.readObject();
        if (!LocalJavaCompilation.INSTANCE.compile(javaCompilationUnit)) {
            System.exit(1);
        }
    }
}
