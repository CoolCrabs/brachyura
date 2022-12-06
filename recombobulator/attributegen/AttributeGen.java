import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * 🍝🍝🍝🍝🍝🍝🍝🍝🍝🍝
 */
public class AttributeGen {
    TreeMap<String, Attribute> attribute = new TreeMap<>();
    static Path out = Paths.get("").resolve("out");
    static Path attributes = out.resolve("attribute");
    static {
        try {
            Files.createDirectories(out);
            Files.createDirectories(attributes);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    static class Attribute {
        EnumSet<Location> locations = EnumSet.noneOf(Location.class);
        int major;
        int minor;
        ArrayList<AttributeField> fields;

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof Attribute) {
                Attribute o = (Attribute) obj;
                return locations.equals(o.locations) && major == o.major && minor == o.minor && fields.equals(o.fields);
            }
            return false;
        }
    }

    static class AttributeField {
        AttributeType type;
        String name;

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof AttributeField) {
                AttributeField o = (AttributeField) obj;
                return type.equals(o.type) && name.equals(o.name);
            }
            return false;
        }
    }

    static interface AttributeType {

    }

    static class UnsignedIntType implements AttributeType {
        int size;

        String type() {
            if (size == 1) return "byte";
            if (size == 2) return "int";
            if (size == 4) return "long";
            throw new RuntimeException("" + size);
        }

        String write(String yeet) {
            if (size == 1) {
                return "o.writeByte((byte)" + yeet + ");";
            } else if (size == 2) {
                return "o.writeShort((short) " + yeet + ");";
            } else if (size == 4) {
                return "o.writeInt((int) " + yeet + ");";
            } else {
                throw new UnsupportedOperationException();
            }
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof UnsignedIntType) {
                UnsignedIntType o = (UnsignedIntType) obj;
                return size == o.size;
            }
            return false;
        }
    }

    static class ArrayType implements AttributeType {
        AttributeField size;
        AttributeType type;

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof ArrayType) {
                ArrayType o = (ArrayType) obj;
                return size.equals(o.size) && type.equals(o.type);
            }
            return false;
        }
    }

    static class OtherType implements AttributeType {
        String name;

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof OtherType) {
                OtherType o = (OtherType) obj;
                return name.equals(o.name);
            }
            return false;
        }
    }

    static class StructType implements AttributeType {
        String name;
        ArrayList<AttributeField> fields = new ArrayList<>();

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof StructType) {
                StructType o = (StructType) obj;
                return name.equals(o.name) && fields.equals(o.fields);
            }
            return false;
        }
    }

    static final HashMap<String, Location> locmap = new HashMap<>();

    enum Location {
        CLASS_FILE("ClassFile"),
        FIELD_INFO("field_info"),
        METHOD_INFO("method_info"),
        CODE("Code"),
        RECORD_COMPONENT("record_component_info");

        final String n;

        Location(String n) {
            this.n = n;
            locmap.put(n, this);
        }
    }

    void parseTsv() throws IOException {
        try (BufferedReader r = Files.newBufferedReader(Paths.get("").resolve("attributes.tsv"))) {
            if (!"Attribute\tLocation\tclass file".equals(r.readLine())) throw new RuntimeException();
            String line;
            while ((line = r.readLine()) != null) {
                String[] strings = line.split("\t");
                String[] names = strings[0].split(",");
                for (int i = 0; i < names.length; i++) {
                    names[i] = names[i].trim();
                }
                String[] locations = strings[1].split(",");
                for (int i = 0; i < locations.length; i++) {
                    locations[i] = locations[i].trim();
                }
                String[] classfile = strings[2].split("\\.");
                int major;
                int minor;
                if (classfile.length == 2) {
                    major = Integer.parseInt(classfile[0]);
                    minor = Integer.parseInt(classfile[1]);
                } else if (classfile.length == 1) {
                    major = Integer.parseInt(classfile[0]);
                    minor = 0;
                } else {
                    throw new RuntimeException("" + classfile.length);
                }
                for (String name : names) {
                    Attribute a = attribute.computeIfAbsent(name, k -> new Attribute());
                    for (String location : locations) {
                        a.locations.add(locmap.get(location));
                    }
                    a.major = major;
                    a.minor = minor;
                }
            }
        }
    }

    // stack_map_frame -> StackMapFrame
    String snekToPascal(String in) {
        StringBuilder out = new StringBuilder(in.length());
        boolean flag = true;
        for (char c : in.toCharArray()) {
            if (flag) {
                out.append(Character.toUpperCase(c));
                flag = false;
            } else {
                if (c == '_') {
                    flag = true;
                } else {
                    out.append(c);
                }
            }
        }
        return out.toString();
    }

    void parseAttributes() throws IOException {
        ArrayDeque<Object> queue = new ArrayDeque<>();
        StringBuilder buf = new StringBuilder();
        String attributeName = null;
        StructType struct = null;
        ArrayDeque<AttributeField> fields = new ArrayDeque<>();
        try (BufferedReader reader = Files.newBufferedReader(Paths.get("").resolve("attributes.txt"))) {
            int r;
            while ((r = reader.read()) >= 0) {
                if (Character.isWhitespace(r)) {
                    if (buf.length() > 0) {
                        queue.add(buf.toString());
                        buf.setLength(0);
                    }
                } else if (r == ';') {
                    if (buf.length() > 0) {
                        queue.add(buf.toString());
                        buf.setLength(0);
                    }
                    Object type = queue.pop();
                    String name = (String) queue.pop();
                    AttributeType atype;
                    if (type instanceof String) {
                        String ts = (String) type;
                        if (ts.length() == 2 && ts.charAt(0) == 'u') {
                            int size = ts.charAt(1) - '0';
                            atype = new UnsignedIntType();
                            ((UnsignedIntType)atype).size = size;
                        } else {
                            System.out.println(ts);
                            atype = new OtherType();
                            ((OtherType)atype).name = snekToPascal(ts);
                        }
                    } else if (type instanceof AttributeType) {
                        atype = (AttributeType) type;
                    } else {
                        throw new RuntimeException(type.toString());
                    }
                    if (name.contains("[")) {
                        name = name.substring(0, name.indexOf('['));
                        ArrayType arrayType = new ArrayType();
                        arrayType.size = struct == null ? fields.removeLast() : struct.fields.remove(struct.fields.size() - 1);
                        if (!(arrayType.size.type instanceof UnsignedIntType)) {
                            System.out.println("panic");
                        }
                        arrayType.type = atype;
                        if (arrayType.type instanceof StructType) {
                            ((StructType)arrayType.type).name = "Entry" + snekToPascal(name);
                        }
                        atype = arrayType;
                    }
                    AttributeField f = new AttributeField();
                    f.name = name;
                    f.type = atype;
                    if (struct == null) {
                        fields.add(f);
                    } else {
                        struct.fields.add(f);
                    }
                } else if (r == '{') {
                    if (attributeName == null) {
                        attributeName = (String) queue.pop();
                    } else {
                        struct = new StructType();
                    }
                } else if (r == '}') {
                    if (struct != null) {
                        queue.push(struct);
                        struct = null;
                    } else {
                        Attribute a = attribute.get(attributeName.replace("_attribute", ""));
                        a.fields = new ArrayList<>(fields);
                        attributeName = null;
                        fields = new ArrayDeque<>();
                    }
                } else {
                    buf.append((char) r);
                }
            }
        }
    }

    ArrayList<StructType> structs = new ArrayList<>();

    void writeClass(String name, String major, String minor, String locations, List<AttributeField> fields, boolean attribute) throws IOException {
        Path template = Paths.get("").resolve(attribute ? "template.txt" : "template2.txt");
        String className = name;
        try (
            BufferedReader r = Files.newBufferedReader(template);
            BufferedWriter w = Files.newBufferedWriter(attributes.resolve(className + ".java"))
        ) {
            ArrayList<AttributeField> instanceFields = new ArrayList<>();
            ArrayList<String> sizes = new ArrayList<>();
            ArrayList<String> hashCodes = new ArrayList<>();
            ArrayList<String> equals = new ArrayList<>();
            ArrayList<String> writes = new ArrayList<>();
            ArrayList<String> visits = new ArrayList<>();
            String line;
            while ((line = r.readLine()) != null) {
                if (line.equals("&&&Read")) {
                    for (AttributeField f : fields) {
                        if (f.name.equals("attribute_length")) continue;
                        instanceFields.add(f);
                        if (f.name.equals("attribute_name_index")) continue;
                        if (f.type instanceof UnsignedIntType) {
                            UnsignedIntType uit = (UnsignedIntType) f.type;
                            w.write("            " + uit.type() + " " + f.name + " = ByteBufferUtil.u" + uit.size + "(b, pos);\n");
                            w.write("            pos += " + uit.size + ";\n");
                            sizes.add("size += " + uit.size + "; // " + f.name);
                            hashCodes.add("(int) " + f.name);
                            equals.add(f.name + " == o." + f.name);
                            if (uit.size == 1) {
                                writes.add("o.writeByte((byte) " + f.name + ");");
                            } else if (uit.size == 2) {
                                writes.add("o.writeShort((short) " + f.name + ");");
                            } else if (uit.size == 4) {
                                writes.add("o.writeInt(" + f.name + ");");
                            } else {
                                throw new UnsupportedOperationException();
                            }
                        } else if (f.type instanceof ArrayType) {
                            ArrayType at = (ArrayType) f.type;
                            UnsignedIntType uit = (UnsignedIntType) at.size.type;
                            if (
                                !at.size.name.equals("attribute_length") &&
                                !(at.type instanceof OtherType && ((OtherType)at.type).name.equals("AttributeInfo"))
                            ) {
                                String readSize;
                                if (uit.size == 1) {
                                    readSize = "b.get(pos)";
                                } else if (uit.size == 4) {
                                    readSize = "b.getInt(pos)";
                                } else {
                                    readSize = "ByteBufferUtil.u" + uit.size + "(b, pos)";
                                }
                                w.write("            int " + at.size.name + " = " + readSize + ";\n");
                                w.write("            pos += " + uit.size + ";\n");
                                sizes.add("size += " + uit.size + "; // " + at.size.name);
                            }
                            if (at.type instanceof UnsignedIntType) {
                                UnsignedIntType uit2 = (UnsignedIntType) at.type;
                                if (uit2.size == 1) {
                                    w.write("            ByteBuffer " + f.name + " = ByteBufferUtil.slice(b, pos, pos + " + at.size.name + ");\n");
                                    w.write("            pos += " + at.size.name + ";\n");
                                    sizes.add("size += " + f.name + ".remaining();");
                                    hashCodes.add(f.name + ".hashCode()");
                                    equals.add(f.name + ".equals(o." + f.name + ")");
                                    writes.add(((UnsignedIntType)at.size.type).write(f.name + ".remaining()"));
                                    writes.add("o.writeBytes(" + f.name + ");");
                                } else if (uit2.size == 2) {
                                    w.write("            U2Slice " + f.name + " = new U2Slice(ByteBufferUtil.slice(b, pos, pos + (" + at.size.name + " * 2)));\n");
                                    w.write("            pos += " + at.size.name + " * 2;\n");
                                    sizes.add("size += " + f.name + ".byteSize();");
                                    hashCodes.add(f.name + ".hashCode()");
                                    equals.add(f.name + ".equals(o." + f.name + ")");
                                    writes.add(((UnsignedIntType)at.size.type).write(f.name + ".size()"));
                                    writes.add(f.name + ".write(o);");
                                } else {
                                    throw new RuntimeException();
                                }
                            } else if (at.type instanceof StructType) {
                                StructType st = (StructType) at.type;
                                structs.add(st);
                                w.write("            List<" + st.name + "> " + f.name + " = new ArrayList<>(" + at.size.name + ");\n");
                                w.write("            for (int i = 0; i < " + at.size.name + "; i++) {\n");
                                w.write("                " + st.name + " tmp = " + st.name + ".read(b, pos, attribute_name_index, attribute_length, options, pool, major, minor);\n");
                                w.write("                pos += tmp.byteSize();\n");
                                w.write("                " + f.name + ".add(tmp);\n");
                                w.write("            }\n");
                                sizes.add("for (" + st.name + " tmp : " + f.name + ") size += tmp.byteSize();");
                                hashCodes.add(f.name + ".hashCode()");
                                equals.add(f.name + ".equals(o." + f.name + ")");
                                writes.add(((UnsignedIntType)at.size.type).write(f.name + ".size()"));
                                writes.add("for (" + st.name + " tmp : " + f.name + ") tmp.write(o);");
                                visits.add("for (" + st.name + " tmp : " + f.name + ") tmp.accept(v);");
                            } else if (at.type instanceof OtherType) {
                                OtherType ot = (OtherType) at.type;
                                if ("AttributeInfo".equals(ot.name)) {
                                    w.write("            Attributes " + f.name + " = Attributes.read(b, pos, options, pool, major, minor, AttributeType.Location.CODE);\n");
                                    w.write("            pos = " + f.name + ".readEnd();\n");
                                    sizes.add("size += " + f.name + ".byteSize();");
                                    hashCodes.add(f.name + ".hashCode()");
                                    equals.add(f.name + ".equals(o." + f.name + ")");
                                    writes.add(f.name + ".write(o);");
                                    visits.add("for (int i = 0; i < " + f.name + ".size(); i++) " + f.name + ".get(i).accept(v);");
                                } else if ("Annotation".equals(ot.name)) {
                                    w.write("            List<Annotation> " + f.name + " = new ArrayList<>(" + at.size.name + ");\n");
                                    w.write("            for (int i = 0; i < " + at.size.name + "; i++) {\n");
                                    w.write("                Annotation tmp = Annotation.read(b, pos);\n");
                                    w.write("                pos += tmp.byteSize();\n");
                                    w.write("                " + f.name + ".add(tmp);\n");
                                    w.write("            }\n");
                                    sizes.add("for (Annotation tmp : " + f.name + ") size += tmp.byteSize();");
                                    hashCodes.add(f.name + ".hashCode()");
                                    equals.add(f.name + ".equals(o." + f.name + ")");
                                    writes.add(((UnsignedIntType)at.size.type).write(f.name + ".size()"));
                                    writes.add("for (Annotation tmp : " + f.name + ") tmp.write(o);");
                                    visits.add("for (Annotation tmp : " + f.name + ") tmp.accept(v);");
                                } else if ("RecordComponentInfo".equals(ot.name)) {
                                    w.write("            List<RecordComponentInfo> " + f.name + " = new ArrayList<>(" + at.size.name + ");\n");
                                    w.write("            for (int i = 0; i < " + at.size.name + "; i++) {\n");
                                    w.write("                RecordComponentInfo tmp = new RecordComponentInfo();\n");
                                    w.write("                pos = tmp.read(b, pos, attribute_name_index, attribute_length, options, pool, major, minor);\n");
                                    w.write("                " + f.name + ".add(tmp);\n");
                                    w.write("            }\n");
                                    sizes.add("for (RecordComponentInfo tmp : " + f.name + ") size += tmp.byteSize();");
                                    hashCodes.add(f.name + ".hashCode()");
                                    equals.add(f.name + ".equals(o." + f.name + ")");
                                    writes.add(((UnsignedIntType)at.size.type).write(f.name + ".size()"));
                                    writes.add("for (RecordComponentInfo tmp : " + f.name + ") tmp.write(o);");
                                    visits.add("for (RecordComponentInfo tmp : " + f.name + ") tmp.accept(v);");
                                } else if ("TypeAnnotation".equals(ot.name)) {
                                    w.write("            List<TypeAnnotation> " + f.name + " = new ArrayList<>(" + at.size.name + ");\n");
                                    w.write("            for (int i = 0; i < " + at.size.name + "; i++) {\n");
                                    w.write("                TypeAnnotation tmp = new TypeAnnotation();\n");
                                    w.write("                pos = tmp.read(b, pos);\n");
                                    w.write("                " + f.name + ".add(tmp);\n");
                                    w.write("            }\n");
                                    sizes.add("for (TypeAnnotation tmp : " + f.name + ") size += tmp.byteSize();");
                                    hashCodes.add(f.name + ".hashCode()");
                                    equals.add(f.name + ".equals(o." + f.name + ")");
                                    writes.add(((UnsignedIntType)at.size.type).write(f.name + ".size()"));
                                    writes.add("for (TypeAnnotation tmp : " + f.name + ") tmp.write(o);");
                                    visits.add("for (TypeAnnotation tmp : " + f.name + ") tmp.accept(v);");
                                } else if ("StackMapFrame".equals(ot.name)) {
                                    w.write("            List<StackMapFrame> " + f.name + " = new ArrayList<>(" + at.size.name + ");\n");
                                    w.write("            pos += StackMapFrame.read(b, pos, " + at.size.name + ", " + f.name + ");\n");
                                    sizes.add("for (StackMapFrame tmp : " + f.name + ") size += tmp.byteSize();");
                                    hashCodes.add(f.name + ".hashCode()");
                                    equals.add(f.name + ".equals(o." + f.name + ")");
                                    writes.add(((UnsignedIntType)at.size.type).write(f.name + ".size()"));
                                    writes.add("for (StackMapFrame tmp : " + f.name + ") tmp.write(o);");
                                    visits.add("for (StackMapFrame tmp : " + f.name + ") tmp.accept(v);");
                                } else {
                                    w.write("// Fail " + ((OtherType)at.type).name + "\n");
                                }
                            }
                        } else if (f.type instanceof OtherType) {
                            OtherType ot = (OtherType) f.type;
                            if ("ElementValue".equals(ot.name)) {
                                w.write("            ElementValue " + f.name + " = ElementValue.read(b, pos);\n");
                                w.write("            pos += " + f.name + ".byteSize();\n");
                                sizes.add("size += " + f.name + ".byteSize();");
                                hashCodes.add(f.name + ".hashCode()");
                                equals.add(f.name + ".equals(o." + f.name + ")");
                                writes.add(f.name + ".write(o);");
                                visits.add(f.name + ".accept(v);");
                            } else {
                                w.write("// Fail ");
                                w.write(Objects.toString(f.type));
                                w.write('\n');
                            }
                        } else {
                            w.write("// Fail ");
                            w.write(Objects.toString(f.type));
                            w.write('\n');
                        }
                    }
                    w.write("            return new " + className + "(\n");
                    for (int i = 0; i < instanceFields.size(); i++) {
                        AttributeField f = instanceFields.get(i);
                        w.write("                ");
                        w.write(f.name);
                        if (i != instanceFields.size() - 1) {
                            w.write(',');
                        }
                        w.write('\n');
                    }
                    w.write("            );\n");
                } else if (line.equals("&&&Body")) {
                    int i = 0;
                    if (instanceFields.get(0).name.equals("attribute_name_index")) i = 1;
                    StringBuilder constructorParams = new StringBuilder();
                    if (attribute) constructorParams.append("int attribute_name_index");
                    for (; i < instanceFields.size(); i++) {
                        AttributeField f = instanceFields.get(i);
                        if (f.name.equals("attribute_length")) continue;
                        if (constructorParams.length() != 0) constructorParams.append(", ");
                        if (f.type instanceof UnsignedIntType) {
                            w.write("    public " + ((UnsignedIntType)f.type).type() + " " + f.name + ";\n");
                            constructorParams.append(((UnsignedIntType)f.type).type() + " " + f.name);
                        } else if (f.type instanceof ArrayType) {
                            ArrayType at = (ArrayType) f.type;
                            if (at.type instanceof UnsignedIntType) {
                                UnsignedIntType uit2 = (UnsignedIntType) at.type;
                                if (uit2.size == 1) {
                                    w.write("    public ByteBuffer " + f.name + ";\n");
                                    constructorParams.append("ByteBuffer " + f.name);
                                } else if (uit2.size == 2) {
                                    w.write("    public U2Slice " + f.name + ";\n");
                                    constructorParams.append("U2Slice " + f.name);
                                } else {
                                    throw new RuntimeException();
                                }
                            } else if (at.type instanceof StructType) {
                                StructType st = (StructType) at.type;
                                w.write("    public List<" + st.name + "> " + f.name + ";\n");
                                constructorParams.append("List<" + st.name + "> " + f.name);
                            } else if (at.type instanceof OtherType) {
                                OtherType ot = (OtherType) at.type;
                                if ("AttributeInfo".equals(ot.name)) {
                                    w.write("    public Attributes " + f.name + ";\n");
                                    constructorParams.append("Attributes " + f.name);
                                } else if (
                                    "RecordComponentInfo".equals(ot.name) ||
                                    "TypeAnnotation".equals(ot.name) ||
                                    "StackMapFrame".equals(ot.name) ||
                                    "Annotation".equals(ot.name)
                                ) {
                                    w.write("    public List<" + ot.name + "> " + f.name + ";\n");
                                    constructorParams.append("List<" + ot.name + "> " + f.name);
                                }
                            }
                        } else if (f.type instanceof OtherType) {
                            OtherType ot = (OtherType) f.type;
                            if ("ElementValue".equals(ot.name)) {
                                w.write("    public ElementValue " + f.name + ";\n");
                                constructorParams.append("ElementValue " + f.name);
                            }
                        } else {
                            w.write("// Fail ");
                            w.write(Objects.toString(f.type));
                            w.write('\n');
                        }
                    }
                    w.write('\n');
                    w.write("    public " + className + "(" + constructorParams + ") {\n");
                    if (attribute) w.write("        super(attribute_name_index);\n");
                    for (AttributeField f : instanceFields) {
                        if (f.name.equals("attribute_name_index")) continue;
                        if (f.name.equals("attribute_length")) continue;
                        w.write("        this." + f.name + " = " + f.name + ";\n");
                    }
                    w.write("    }\n");
                } else if (line.equals("&&&Size")) {
                    for (String s : sizes) {
                        w.write("        ");
                        w.write(s);
                        w.write('\n');
                    }
                } else if (line.equals("&&&Equals")) {
                    for (String s : equals) {
                        w.write("                ");
                        w.write(s);
                        w.write(" &&\n");
                    }
                } else if (line.equals("&&&HashCode")) {
                    w.write("        int result = 17;\n");
                    for (String s : hashCodes) {
                        w.write("        result = 37*result + ");
                        w.write(s);
                        w.write(";\n");
                    }
                    w.write("        return result;\n");
                } else if (line.equals("&&&Write")) {
                    for (String s : writes) {
                        w.write("        ");
                        w.write(s);
                        w.write('\n');
                    }
                } else if (line.equals("&&&Visit")) {
                    w.write("        v.visit" + className + "(this);\n");
                    for (String s : visits) {
                        w.write("        ");
                        w.write(s);
                        w.write('\n');
                    }
                } else {
                    w.write(
                        line.replace("$$$ClassName", className)
                            .replace("$$$Major", major)
                            .replace("$$$Minor", minor)
                            .replace("$$$Locations", locations)
                            .replace("$$$Name", name.replace("Attribute", ""))
                    );
                    w.write('\n');
                }
            }
        }
    }

    void writeGenAttributeMap() throws IOException {
        try (
            BufferedReader r = Files.newBufferedReader(Paths.get("").resolve("template3.txt"));
            BufferedWriter w = Files.newBufferedWriter(attributes.resolve("GenAttributeMap.java"));
        ) {
            String line;
            while ((line = r.readLine()) != null) {
                if (line.equals("&&&Bruh")) {
                    for (Map.Entry<String, Attribute> entry : attribute.entrySet()) {
                        String clsname = "Attribute" + entry.getKey();
                        w.write("        attributeMap.put(" + clsname + ".NAME, " + clsname + ".TYPE);\n");
                    }
                } else {
                    w.write(line);
                    w.write('\n');
                }
            }
        }
    }

    void writeVisitor(List<String> classes) throws IOException {
        try (
            BufferedReader r = Files.newBufferedReader(Paths.get("").resolve("template4.txt"));
            BufferedWriter w = Files.newBufferedWriter(out.resolve("RecombobulatorVisitor.java"));
        ) {
            String line;
            while ((line = r.readLine()) != null) {
                if (line.equals("&&&Imports")) {
                    for (String cls : classes) {
                        if (!(cls.equals("ClassInfo") || cls.equals("MethodInfo") || cls.equals("FieldInfo"))) {
                            if ("LocalvarTableEntry".equals(cls)) {
                                cls = "TargetLocalvar.LocalvarTableEntry";
                            }
                            w.write("import io.github.coolcrabs.brachyura.recombobulator.attribute." + cls + ";\n");
                        }
                    }
                } else if (line.equals("&&&Methods")) {
                    for (String cls : classes) {
                        w.write("    void visit" + cls + "(" + cls + " el);\n");
                    }
                } else {
                    w.write(line);
                    w.write('\n');
                }
            }
        }
    }

    public static void main(String[] args) throws IOException {
        ArrayList<String> classes = new ArrayList<>(Arrays.asList(
            "ClassInfo",
            "MethodInfo",
            "FieldInfo",
            "RecordComponentInfo",
            // "ConstantUtf8",
            // "ConstantInteger",
            // "ConstantFloat",
            // "ConstantLong",
            // "ConstantDouble",
            // "ConstantClass",
            // "ConstantString",
            // "ConstantFieldref",
            // "ConstantMethodref",
            // "ConstantInterfaceMethodref",
            // "ConstantNameAndType",
            // "ConstantMethodType",
            // "ConstantDynamic",
            // "ConstantInvokeDynamic",
            // "ConstantModule",
            // "ConstantPackage",
            "Annotation",
            "ElementValuePair",
            "ElementValueConst",
            "ElementValueEnum",
            "ElementValueClass",
            "ElementValueAnnotation",
            "ElementValueArray",
            "TypeAnnotation",
            "TargetTypeParameter",
            "TargetSupertype",
            "TargetTypeParameterBound",
            "TargetEmpty",
            "TargetFormalParameter",
            "TargetThrows",
            "TargetLocalvar",
            "LocalvarTableEntry",
            "TargetCatch",
            "TargetOffset",
            "TargetTypeArgument",
            "TypePath",
            "EntryPath",
            "SMFrameSame",
            "SMFrameSameLocals1StackItem",
            "SMFrameExtendedSameLocals1StackItem",
            "SMFrameChop",
            "SMFrameExtendedSame",
            "SMFrameAppend",
            "SMFrameFull",
            "VerificationTypeNonreference",
            "VerificationTypeObject",
            "VerificationTypeUninitialized",
            "AttributeUnknown"
        ));
        AttributeGen thiz = new AttributeGen();
        thiz.parseTsv();
        thiz.parseAttributes();
        for (Map.Entry<String, Attribute> entry : thiz.attribute.entrySet()) {
            System.out.println(entry.getKey() + " " + entry.getValue().toString());
            Attribute a = entry.getValue();
            String major = String.valueOf(a.major);
            String minor = String.valueOf(a.minor);
            String locations = a.locations.stream().map(l -> "AttributeType.Location." + l.toString()).collect(Collectors.joining(", "));
            thiz.writeClass("Attribute" + entry.getKey(), major, minor, locations, a.fields, true);
            classes.add("Attribute" + entry.getKey());
        }
        HashMap<String, StructType> ah = new HashMap<>();
        for (StructType type : thiz.structs) {
            StructType old = ah.put(type.name, type);
            if (old != null && !old.equals(type)) {
                throw new RuntimeException(old.name);
            }
        }
        for (StructType type : ah.values()) {
            thiz.writeClass(type.name, "", "", "", type.fields, false);
            classes.add(type.name);
        }
        thiz.writeGenAttributeMap();
        thiz.writeVisitor(classes);
        System.out.println("done");
    }
}
