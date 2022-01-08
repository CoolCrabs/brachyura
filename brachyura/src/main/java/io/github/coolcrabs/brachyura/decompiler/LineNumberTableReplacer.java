package io.github.coolcrabs.brachyura.decompiler;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import java.util.Map;

import io.github.coolcrabs.brachyura.decompiler.DecompileLineNumberTable.ClassLineMap;
import io.github.coolcrabs.brachyura.decompiler.DecompileLineNumberTable.MethodId;
import io.github.coolcrabs.brachyura.util.AtomicFile;
import io.github.coolcrabs.brachyura.util.FileSystemUtil;
import io.github.coolcrabs.brachyura.util.PathUtil;
import io.github.coolcrabs.brachyura.util.Util;
import org.tinylog.Logger;

// Basically a mini class file reader + writer
// Supports round trip with no unneeded changes
// ow2 asm didn't have good apis for what I needed
public class LineNumberTableReplacer {

    static final String CODE_UTF8 = "Code";
    static final String LINE_NUMBER_TABLE_UTF8 = "LineNumberTable";

    public static void replaceLineNumbers(Path sourceJar, Path targetJar, DecompileLineNumberTable table) {
        try {
            try (AtomicFile af = new AtomicFile(targetJar)) {
                Files.deleteIfExists(af.tempPath);
                try (
                    FileSystem source = FileSystemUtil.newJarFileSystem(sourceJar);
                    FileSystem target = FileSystemUtil.newJarFileSystem(af.tempPath);
                ) {
                    Files.walkFileTree(source.getPath("/"), new SimpleFileVisitor<Path>() {
                        @Override
                        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                            if (file.toString().endsWith(".class")) {
                                int lineNumberConstIndex = -1;
                                ClassFile c;
                                try (DataInputStream in = new DataInputStream(PathUtil.inputStream(file))) {
                                    c = readClassFile(in);
                                }
                                String cName = ((ConstantUtf8)cpEntry(c.constantPool, ((ConstantClass) cpEntry(c.constantPool, c.thisClass)).nameIndex)).data;
                                ClassLineMap mmap = table.classes.get(cName);
                                if (mmap != null) {
                                    for (Method method : c.methods) {
                                        MethodId mid = new MethodId(((ConstantUtf8) cpEntry(c.constantPool, method.nameIndex)).data, ((ConstantUtf8) cpEntry(c.constantPool, method.descriptorIndex)).data);
                                        DecompileLineNumberTable.MethodLineMap mln = mmap == null ? null : mmap.methods.get(mid);
                                        for (Attribute attr : method.attributes) {
                                            if (attr instanceof AttributeCode) {
                                                AttributeCode ac = (AttributeCode) attr;
                                                if (mln != null && mln.isReplace) {
                                                    AttributeLineNumberTable lnAttr = null;
                                                    boolean foundFirstLnTable = false;
                                                    for (int i = 0; i < ac.attributes.length; i++) {
                                                        if (ac.attributes[i] instanceof AttributeLineNumberTable) {
                                                            if (foundFirstLnTable) {
                                                                ((AttributeLineNumberTable)ac.attributes[i]).lineNumberTable = new LineNumberTableEntry[0];
                                                            } else {
                                                                lnAttr = (AttributeLineNumberTable) ac.attributes[i];
                                                                foundFirstLnTable = true;
                                                            }
                                                        }
                                                    }
                                                    if (!foundFirstLnTable) {
                                                        ac.attributes = Arrays.copyOf(ac.attributes, ac.attributes.length + 1);
                                                        ac.attributes[ac.attributes.length - 1] = lnAttr = new AttributeLineNumberTable();
                                                        if (lineNumberConstIndex == -1) {
                                                            lineNumberConstIndex = getOrCreateUtf8Const(LINE_NUMBER_TABLE_UTF8, c);
                                                        }
                                                    }

                                                    lnAttr.lineNumberTable = mln.replace.toArray(new LineNumberTableEntry[mln.replace.size()]); 
                                                } else {
                                                    Map<Integer, Integer> remap = mmap.isStupid ? mmap.stupid : mln.remap;
                                                    for (int i = 0; i < ac.attributes.length; i++) {
                                                        if (ac.attributes[i] instanceof AttributeLineNumberTable) {
                                                            LineNumberTableEntry[] lnt = ((AttributeLineNumberTable)ac.attributes[i]).lineNumberTable;
                                                            for (int j = 0; j < lnt.length; j++) {
                                                                Integer rmp = remap.get((int)lnt[j].lineNumber);
                                                                if (rmp != null) lnt[j] = new LineNumberTableEntry(lnt[j].startPc, rmp);
                                                                else Logger.warn("Missing remap {} in {}", lnt[j].lineNumber, file);
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                                Path outPath = target.getPath(file.toString());
                                try (DataOutputStream out = new DataOutputStream(PathUtil.outputStream(outPath))) {
                                    writeClassFile(out, c);
                                }
                            } else {
                                Path targetPath = target.getPath(file.toString());
                                Files.createDirectories(targetPath.getParent());
                                Files.copy(file, targetPath);
                            }
                            return FileVisitResult.CONTINUE;
                        }
                    });
                }
                af.commit();
            }
        } catch (Exception e) {
            throw Util.sneak(e);
        }
    }
    
    static int getOrCreateUtf8Const(String constant, ClassFile clazz) {
        for (int i = 0; i < clazz.constantPool.length; i++) {
            Constant c = clazz.constantPool[i];
            if (c instanceof ConstantUtf8) {
                ConstantUtf8 utf8 = (ConstantUtf8) c;
                if (constant.equals(utf8.data)) {
                    return i;
                }
            }
        }
        clazz.constantPool = Arrays.copyOf(clazz.constantPool, clazz.constantPool.length + 1);
        int r = clazz.constantPool.length - 1;
        ConstantUtf8 newConst = new ConstantUtf8();
        newConst.tag = 1;
        newConst.data = constant;
        clazz.constantPool[r] = newConst;
        return r;
    }
    
    static class ClassFile {
        int magic;
        short minorVersion;
        short majorVersion;
        Constant[] constantPool;
        short accessFlags;
        short thisClass;
        short superClass;
        short[] interfaces;
        Field[] fields;
        Method[] methods;
        Attribute[] attributes;
    }

    static Constant cpEntry(Constant[] cp, short index) {
        return cp[Short.toUnsignedInt(index) - 1];
    }

    static ClassFile readClassFile(DataInputStream in) throws IOException {
        ClassFile result = new ClassFile();
        result.magic = in.readInt();
        result.minorVersion = in.readShort();
        result.majorVersion = in.readShort();
        short cpCount = in.readShort();
        result.constantPool = new Constant[cpCount - 1];
        for (int i = 0; i < cpCount - 1; i++) {
            Constant constant = readConstant(in);
            result.constantPool[i] = constant;
            if (constant instanceof ConstantU8) {
                ++i; // dumb
            }
        }
        result.accessFlags = in.readShort();
        result.thisClass = in.readShort();
        result.superClass = in.readShort();
        result.interfaces = new short[in.readShort()];
        for (int i = 0; i < result.interfaces.length; i++) {
            result.interfaces[i] = in.readShort();
        }
        result.fields = new Field[in.readShort()];
        for (int i = 0; i < result.fields.length; i++) {
            result.fields[i] = readField(in, result.constantPool);
        }
        result.methods = new Method[in.readShort()];
        for (int i = 0; i < result.methods.length; i++) {
            result.methods[i] = readMethod(in, result.constantPool);
        }
        result.attributes = new Attribute[in.readShort()];
        for (int i = 0; i < result.attributes.length; i++) {
            result.attributes[i] = readAttribute(in, result.constantPool, AttributeType.OTHER);
        }
        return result;
    }

    static void writeClassFile(DataOutputStream out, ClassFile c) throws IOException {
        out.writeInt(c.magic);
        out.writeShort(c.minorVersion);
        out.writeShort(c.majorVersion);
        out.writeShort(c.constantPool.length + 1);
        for (int i = 0; i < c.constantPool.length; i++) {
            Constant constant = c.constantPool[i];
            if (constant != null) {
                writeConstant(out, constant);
            }
        }
        out.writeShort(c.accessFlags);
        out.writeShort(c.thisClass);
        out.writeShort(c.superClass);
        out.writeShort(c.interfaces.length);
        for (int i = 0; i < c.interfaces.length; i++) {
            out.writeShort(c.interfaces[i]);
        }
        out.writeShort(c.fields.length);
        for (int i = 0; i < c.fields.length; i++) {
            writeField(out, c.fields[i]);
        }
        out.writeShort(c.methods.length);
        for (int i = 0; i < c.methods.length; i++) {
            writeMethod(out, c.methods[i]);
        }
        out.writeShort(c.attributes.length);
        for (int i = 0; i < c.attributes.length; i++) {
            writeAttribute(out, c.attributes[i]);
        }
    }

    enum AttributeType {
        METHOD,
        CODE,
        OTHER
    }

    static Attribute readAttribute(DataInputStream in, Constant[] cp, AttributeType attributeType) throws IOException {
        short attributeNameIndex = in.readShort();
        int attributeLength = in.readInt();
        ConstantUtf8 c = (ConstantUtf8) cpEntry(cp, attributeNameIndex);
        Attribute result;
        if (attributeType == AttributeType.METHOD && CODE_UTF8.equals(c.data)) {
            AttributeCode result1;
            result = result1 = new AttributeCode();
            result1.maxStack = in.readShort();
            result1.maxLocals = in.readShort();
            result1.code = new byte[in.readInt()];
            in.readFully(result1.code);
            result1.exceptionTable = new AttributeCode.ExceptionTableEntry[in.readShort()];
            for (int i = 0; i < result1.exceptionTable.length; i++) {
                AttributeCode.ExceptionTableEntry e = new AttributeCode.ExceptionTableEntry();
                e.startPc = in.readShort();
                e.endPc = in.readShort();
                e.handlerPc = in.readShort();
                e.catchType = in.readShort();
                result1.exceptionTable[i] = e;
            }
            result1.attributes = new Attribute[in.readShort()];
            for (int i = 0; i < result1.attributes.length; i++) {
                result1.attributes[i] = readAttribute(in, cp, AttributeType.CODE);
            }
        } else if (attributeType == AttributeType.CODE && LINE_NUMBER_TABLE_UTF8.equals(c.data)) {
            AttributeLineNumberTable result2;
            result = result2 = new AttributeLineNumberTable();
            result2.lineNumberTable = new LineNumberTableEntry[in.readShort()];
            for (int i = 0; i < result2.lineNumberTable.length; i++) {
                result2.lineNumberTable[i] = new LineNumberTableEntry(in.readShort(), in.readShort());
            }
        } else {
            AttributeUnparsed result0;
            result = result0 = new AttributeUnparsed();
            result0.info = new byte[attributeLength];
            in.readFully(result0.info);
        }
        result.attributeNameIndex = attributeNameIndex;
        return result;
    }

    static void writeAttribute(DataOutputStream out, Attribute a) throws IOException {
        out.writeShort(a.attributeNameIndex);
        if (a instanceof AttributeCode) {
            AttributeCode a0 = (AttributeCode) a;
            ByteArrayOutputStream b = new ByteArrayOutputStream();
            DataOutputStream bd = new DataOutputStream(b);
            bd.writeShort(a0.maxStack);
            bd.writeShort(a0.maxLocals);
            bd.writeInt(a0.code.length);
            bd.write(a0.code);
            bd.writeShort(a0.exceptionTable.length);
            for (int i = 0; i < a0.exceptionTable.length; i++) {
                AttributeCode.ExceptionTableEntry e = a0.exceptionTable[i];
                bd.writeShort(e.startPc);
                bd.writeShort(e.endPc);
                bd.writeShort(e.handlerPc);
                bd.writeShort(e.catchType);
            }
            bd.writeShort(a0.attributes.length);
            for (int i = 0; i < a0.attributes.length; i++) {
                writeAttribute(bd, a0.attributes[i]);
            }
            byte[] attributeInfo = b.toByteArray();
            out.writeInt(attributeInfo.length);
            out.write(attributeInfo);
        } else if (a instanceof AttributeLineNumberTable) {
            AttributeLineNumberTable a0 = (AttributeLineNumberTable) a;
            out.writeInt(a0.lineNumberTable.length * 4 + 2); // attribute_length
            out.writeShort(a0.lineNumberTable.length);
            for (int i = 0; i < a0.lineNumberTable.length; i++) {
                out.writeShort(a0.lineNumberTable[i].startPc);
                out.writeShort(a0.lineNumberTable[i].lineNumber);
            }
        } else if (a instanceof AttributeUnparsed) {
            AttributeUnparsed a0 = (AttributeUnparsed) a;
            out.writeInt(a0.info.length);
            out.write(a0.info);
        } else {
            throw new UnsupportedOperationException("Unknown attribute type: " + a.getClass().getName());
        }
    }   

    static class Attribute {
        short attributeNameIndex;
    }

    static class AttributeCode extends Attribute {
        short maxStack;
        short maxLocals;
        byte[] code;
        ExceptionTableEntry[] exceptionTable;
        Attribute[] attributes;

        static class ExceptionTableEntry {
            short startPc;
            short endPc;
            short handlerPc;
            short catchType;
        }
    }

    static class AttributeLineNumberTable extends Attribute {
        LineNumberTableEntry[] lineNumberTable;
    }

    static class AttributeUnparsed extends Attribute {
        byte[] info;
    }

    static Method readMethod(DataInputStream in, Constant[] cp) throws IOException {
        Method result = new Method();
        result.accessFlags = in.readShort();
        result.nameIndex = in.readShort();
        result.descriptorIndex = in.readShort();
        result.attributes = new Attribute[in.readShort()];
        for (int i = 0; i < result.attributes.length; i++) {
            result.attributes[i] = readAttribute(in, cp, AttributeType.METHOD);
        }
        return result;
    }

    static void writeMethod(DataOutputStream out, Method m) throws IOException {
        out.writeShort(m.accessFlags);
        out.writeShort(m.nameIndex);
        out.writeShort(m.descriptorIndex);
        out.writeShort(m.attributes.length);
        for (int i = 0; i < m.attributes.length; i++) {
            writeAttribute(out, m.attributes[i]);
        }
    }

    static class Method {
        short accessFlags;
        short nameIndex;
        short descriptorIndex;
        Attribute[] attributes;
    }

    static Field readField(DataInputStream in, Constant[] cp) throws IOException {
        Field result = new Field();
        result.accessFlags = in.readShort();
        result.nameIndex = in.readShort();
        result.descriptorIndex = in.readShort();
        result.attributes = new Attribute[in.readShort()];
        for (int i = 0; i < result.attributes.length; i++) {
            result.attributes[i] = readAttribute(in, cp, AttributeType.OTHER);
        }
        return result;
    }

    static void writeField(DataOutputStream out, Field f) throws IOException {
        out.writeShort(f.accessFlags);
        out.writeShort(f.nameIndex);
        out.writeShort(f.descriptorIndex);
        out.writeShort(f.attributes.length);
        for (int i = 0; i < f.attributes.length; i++) {
            writeAttribute(out, f.attributes[i]);
        }
    }

    static class Field {
        short accessFlags;
        short nameIndex;
        short descriptorIndex;
        Attribute[] attributes;
    }

    static Constant readConstant(DataInputStream in) throws IOException {
        byte tag = in.readByte();
        Constant result;
        switch (tag) {
        case 7:
            ConstantClass result2;
            result = result2 = new ConstantClass();
            result2.nameIndex = in.readShort();
            break;
        case 9:
        case 10:
        case 11:
            ConstantRef result3;
            result = result3 = new ConstantRef();
            result3.classIndex = in.readShort();
            result3.nameAndTypeIndex = in.readShort();
            break;
        case 8:
            ConstantString result4;
            result = result4 = new ConstantString();
            result4.stringIndex = in.readShort();
            break;
        case 3:
        case 4:
            ConstantU4 result5;
            result = result5 = new ConstantU4();
            result5.bytes = in.readInt();
            break;
        case 5:
        case 6:
            ConstantU8 result6;
            result = result6 = new ConstantU8();
            result6.bytes = in.readLong();
            break;
        case 12:
            ConstantNameAndType result7;
            result = result7 = new ConstantNameAndType();
            result7.nameIndex = in.readShort();
            result7.descriptorIndex = in.readShort();
            break;
        case 1:
            ConstantUtf8 result8;
            result = result8 = new ConstantUtf8();
            result8.data = in.readUTF();
            break;
        case 15:
            ConstantMethodHandle result9;
            result = result9 = new ConstantMethodHandle();
            result9.referenceKind = in.readByte();
            result9.referenceIndex = in.readShort();
            break;
        case 16:
            ConstantMethodType result10;
            result = result10 = new ConstantMethodType();
            result10.descriptorIndex = in.readShort();
            break;
        case 18:
            ConstantInvokeDynamic result11;
            result = result11 = new ConstantInvokeDynamic();
            result11.bootstrapMethodAttrIndex = in.readShort();
            result11.nameAndTypeIndex = in.readShort();
            break;
        default:
            throw new UnsupportedOperationException("" + tag);
        }
        result.tag = tag;
        return result;
    }

    static void writeConstant(DataOutputStream out, Constant c) throws IOException {
        out.writeByte(c.tag);
        switch (c.tag) {
        case 7:
            out.writeShort(((ConstantClass)c).nameIndex);
            break;
        case 9:
        case 10:
        case 11:
            out.writeShort(((ConstantRef)c).classIndex);
            out.writeShort(((ConstantRef)c).nameAndTypeIndex);
            break;
        case 8:
            out.writeShort(((ConstantString)c).stringIndex);
            break;
        case 3:
        case 4:
            out.writeInt(((ConstantU4)c).bytes);
            break;
        case 5:
        case 6:
            out.writeLong(((ConstantU8)c).bytes);
            break;
        case 12:
            out.writeShort(((ConstantNameAndType)c).nameIndex);
            out.writeShort(((ConstantNameAndType)c).descriptorIndex);
            break;
        case 1:
            out.writeUTF(((ConstantUtf8)c).data);
            break;
        case 15:
            out.writeByte(((ConstantMethodHandle)c).referenceKind);
            out.writeShort(((ConstantMethodHandle)c).referenceIndex);
            break;
        case 16:
            out.writeShort(((ConstantMethodType)c).descriptorIndex);
            break;
        case 18:
            out.writeShort(((ConstantInvokeDynamic)c).bootstrapMethodAttrIndex);
            out.writeShort(((ConstantInvokeDynamic)c).nameAndTypeIndex);
            break;
        default:
            throw new UnsupportedOperationException("" + c.tag);
        }
    }

    static class Constant {
        byte tag;
    }

    static class ConstantClass extends Constant {
        short nameIndex;
    }

    static class ConstantRef extends Constant { // Field, Method and InterfaceMethod ref
        short classIndex;
        short nameAndTypeIndex;
    }

    static class ConstantString extends Constant {
        short stringIndex;
    }

    static class ConstantU4 extends Constant { // int + float
        int bytes;
    }

    static class ConstantU8 extends Constant { // long + double
        long bytes;
    }

    static class ConstantNameAndType extends Constant {
        short nameIndex;
        short descriptorIndex;
    }

    static class ConstantUtf8 extends Constant {
        String data;
    }

    static class ConstantMethodHandle extends Constant {
        byte referenceKind;
        short referenceIndex;
    }

    static class ConstantMethodType extends Constant {
        short descriptorIndex;
    }

    static class ConstantInvokeDynamic extends Constant {
        short bootstrapMethodAttrIndex;
        short nameAndTypeIndex;
    }
}
