package io.github.coolcrabs.cfr.api;

//TODO: Classes are requested in qualified names
public interface BrachyuraCFRJavadocProvider {
    String getClassJavadoc(String clazz);

    String getMethodJavadoc(String clazz, String signature, String methodName);

    String getFieldJavadoc(String clazz, String signature, String fieldName);
}
