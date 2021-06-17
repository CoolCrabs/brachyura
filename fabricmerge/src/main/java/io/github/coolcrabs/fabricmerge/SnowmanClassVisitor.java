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

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

class SnowmanClassVisitor extends ClassVisitor {
	public static class SnowmanMethodVisitor extends MethodVisitor {
		public SnowmanMethodVisitor(int api, MethodVisitor methodVisitor) {
			super(api, methodVisitor);
		}

		@Override
		public void visitParameter(final String name, final int access) {
			if (name != null && name.startsWith("\u2603")) {
				super.visitParameter(null, access);
			} else {
				super.visitParameter(name, access);
			}
		}

		@Override
		public void visitLocalVariable(
				final String name,
				final String descriptor,
				final String signature,
				final Label start,
				final Label end,
				final int index) {
			String newName = name;
			if (name != null && name.startsWith("\u2603")) {
				newName = "lvt" + index;
			}
			super.visitLocalVariable(newName, descriptor, signature, start, end, index);
		}
	}

	public SnowmanClassVisitor(int api, ClassVisitor cv) {
		super(api, cv);
	}

	@Override
	public void visitSource(final String source, final String debug) {
		// Don't trust the obfuscation on this.
		super.visitSource(null, null);
	}

	@Override
	public MethodVisitor visitMethod(
			final int access,
			final String name,
			final String descriptor,
			final String signature,
			final String[] exceptions) {
		return new SnowmanMethodVisitor(api, super.visitMethod(access, name, descriptor, signature, exceptions));
	}
}