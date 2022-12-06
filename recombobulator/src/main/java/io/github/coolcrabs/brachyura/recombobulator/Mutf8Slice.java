package io.github.coolcrabs.brachyura.recombobulator;

import java.nio.ByteBuffer;

/**
 * Represents a mutf8 string
 */
public class Mutf8Slice implements Comparable<Mutf8Slice> {
    public final ByteBuffer b;

    private int hash;
    private boolean hashIsZero;

    private String string;

    public Mutf8Slice(ByteBuffer b) {
        this.b = b;
    }

    // DataOutputStream.writeUtf but without length prefix
    public Mutf8Slice(String str) {
        int strlen = str.length();
        int utflen = 0;
        int c, count = 0;

        /* use charAt instead of copying String to char array */
        for (int i = 0; i < strlen; i++) {
            c = str.charAt(i);
            if ((c >= 0x0001) && (c <= 0x007F)) {
                utflen++;
            } else if (c > 0x07FF) {
                utflen += 3;
            } else {
                utflen += 2;
            }
        }

        if (utflen > 65535)
            throw new IllegalArgumentException(
                "encoded string too long: " + utflen + " bytes");

        byte[] bytearr = new byte[utflen];

        int i=0;
        for (i=0; i<strlen; i++) {
           c = str.charAt(i);
           if (!((c >= 0x0001) && (c <= 0x007F))) break;
           bytearr[count++] = (byte) c;
        }

        for (;i < strlen; i++){
            c = str.charAt(i);
            if ((c >= 0x0001) && (c <= 0x007F)) {
                bytearr[count++] = (byte) c;

            } else if (c > 0x07FF) {
                bytearr[count++] = (byte) (0xE0 | ((c >> 12) & 0x0F));
                bytearr[count++] = (byte) (0x80 | ((c >>  6) & 0x3F));
                bytearr[count++] = (byte) (0x80 | ((c >>  0) & 0x3F));
            } else {
                bytearr[count++] = (byte) (0xC0 | ((c >>  6) & 0x1F));
                bytearr[count++] = (byte) (0x80 | ((c >>  0) & 0x3F));
            }
        }
        this.b = ByteBuffer.wrap(bytearr);
        this.string = str;
    }

    // Mostly copy of java.io.DataInputStream.readUtf
    @Override
    public String toString() {
        if (string == null) {
            int start = b.position();
            int end = b.limit();
            int utflen = end - start;
            char[] chararr = new char[utflen];

            int c, char2, char3;
            int count = 0;
            int chararr_count=0;

            while (count < utflen) {
                c = (int) b.get(count + start) & 0xff;
                if (c > 127) break;
                count++;
                chararr[chararr_count++]=(char)c;
            }

            while (count < utflen) {
                c = (int) b.get(count + start) & 0xff;
                switch (c >> 4) {
                    case 0: case 1: case 2: case 3: case 4: case 5: case 6: case 7:
                        /* 0xxxxxxx*/
                        count++;
                        chararr[chararr_count++]=(char)c;
                        break;
                    case 12: case 13:
                        /* 110x xxxx   10xx xxxx*/
                        count += 2;
                        if (count > utflen)
                            throw new ClassDecodeException(
                                "malformed input: partial character at end");
                        char2 = (int) b.get(count-1 + start);
                        if ((char2 & 0xC0) != 0x80)
                            throw new ClassDecodeException(
                                "malformed input around byte " + count);
                        chararr[chararr_count++]=(char)(((c & 0x1F) << 6) |
                                                        (char2 & 0x3F));
                        break;
                    case 14:
                        /* 1110 xxxx  10xx xxxx  10xx xxxx */
                        count += 3;
                        if (count > utflen)
                            throw new ClassDecodeException(
                                "malformed input: partial character at end");
                        char2 = (int) b.get(count-2 + start);
                        char3 = (int) b.get(count-1 + start);
                        if (((char2 & 0xC0) != 0x80) || ((char3 & 0xC0) != 0x80))
                            throw new ClassDecodeException(
                                "malformed input around byte " + (count-1));
                        chararr[chararr_count++]=(char)(((c     & 0x0F) << 12) |
                                                        ((char2 & 0x3F) << 6)  |
                                                        ((char3 & 0x3F) << 0));
                        break;
                    default:
                        /* 10xx xxxx,  1111 xxxx */
                        throw new ClassDecodeException(
                            "malformed input around byte " + count);
                }
            }
            // The number of chars produced may be less than utflen
            string = new String(chararr, 0, chararr_count);
        }
        return string;
    }

    @Override
    public int hashCode() {
        int h = hash;
        if (h == 0 && !hashIsZero) {
            h = b.hashCode();
            if (h == 0) {
                hashIsZero = true;
            } else {
                hash = h;
            }
        }
        return h;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj instanceof Mutf8Slice) {
            Mutf8Slice o = (Mutf8Slice) obj;
            return b.equals(o.b);
        }
        return false;
    }

    @Override
    public int compareTo(Mutf8Slice o) {
        return b.compareTo(o.b);
    }
}
