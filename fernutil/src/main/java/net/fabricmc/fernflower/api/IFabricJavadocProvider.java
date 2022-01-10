package net.fabricmc.fernflower.api;

import org.jetbrains.java.decompiler.struct.StructClass;
import org.jetbrains.java.decompiler.struct.StructField;
import org.jetbrains.java.decompiler.struct.StructMethod;

public interface IFabricJavadocProvider {
    String PROPERTY_NAME = "fabric:javadoc";
  
    String getClassDoc(StructClass structClass);
  
    String getFieldDoc(StructClass structClass, StructField structField);
  
    String getMethodDoc(StructClass structClass, StructMethod structMethod);
}
