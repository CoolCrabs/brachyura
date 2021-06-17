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

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * ProGuard has a bug where parameter annotations are applied incorrectly in the presence of
 * synthetic arguments. This causes javac to balk when trying to load affected classes.
 *
 * We use several heuristics to guess what the synthetic arguments may be for a particular 
 * constructor. We then check if the constructor matches our guess, and if so, offset all
 * parameter annotations.
 */
class SyntheticParameterClassVisitor extends ClassVisitor {
    private class SyntheticMethodVisitor extends MethodVisitor {
        private final int offset;

        SyntheticMethodVisitor(int api, int offset, MethodVisitor methodVisitor) {
            super(api, methodVisitor);
            this.offset = offset;
        }

        @Override
        public AnnotationVisitor visitParameterAnnotation(int parameter, String descriptor, boolean visible) {
            return super.visitParameterAnnotation(parameter - offset, descriptor, visible);
        }

        @Override
        public void visitAnnotableParameterCount(int parameterCount, boolean visible) {
            super.visitAnnotableParameterCount(parameterCount - offset, visible);
        }
    }

    private String className;
    private int synthetic;
    private String syntheticArgs;
    private boolean backoff = false;

    public SyntheticParameterClassVisitor(int api, ClassVisitor cv) {
        super(api, cv);
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        super.visit(version, access, name, signature, superName, interfaces);

        this.className = name;

        // Enums will always have a string name and then the ordinal
        if ((access & Opcodes.ACC_ENUM) != 0) {
            synthetic = 2;
            syntheticArgs = "(Ljava/lang/String;I";
        }

        if (version >= 55) {
            // Backoff on java 11 or newer due to nest mates being used.
            backoff = true;
        }
    }

    @Override
    public void visitInnerClass(String name, String outerName, String innerName, int access) {
        super.visitInnerClass(name, outerName, innerName, access);

        // If we're a non-static, non-anonymous inner class then we can assume the first argument 
        // is the parent class.
        // See https://docs.oracle.com/javase/specs/jls/se11/html/jls-8.html#jls-8.8.1
        if (synthetic == 0 && name.equals(this.className) && innerName != null && outerName != null && (access & Opcodes.ACC_STATIC) == 0) {
            this.synthetic = 1;
            this.syntheticArgs = "(L" + outerName + ";";
        }
    }

    @Override
    public MethodVisitor visitMethod(
        final int access,
        final String name,
        final String descriptor,
        final String signature,
        final String[] exceptions) {
        MethodVisitor mv = super.visitMethod(access, name, descriptor, signature, exceptions);

        return mv != null && synthetic != 0 && name.equals("<init>") && descriptor.startsWith(syntheticArgs) && !backoff
               ? new SyntheticMethodVisitor(api, synthetic, mv)
               : mv;
    }
}