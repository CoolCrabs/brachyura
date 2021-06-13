package io.github.coolcrabs.cfr.api;

/**
 * Provides javadocs
 * Classes are in internal format (slashed)
 */
public interface BrachyuraCFRJavadocProvider {
    String getClassJavadoc(String clazz);

    String getMethodJavadoc(String clazz, String signature, String methodName);

    String getFieldJavadoc(String clazz, String signature, String fieldName);
}
