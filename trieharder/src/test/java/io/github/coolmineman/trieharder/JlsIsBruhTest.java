package io.github.coolmineman.trieharder;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import org.junit.jupiter.api.Test;

class JlsIsBruhTest {
    @Test
    void bruhBruh() throws IOException {
        String hmm = new String(Base64.getDecoder().decode("XHUwMDJmXHUwMDJmXHUwMDIwXHUwMDQ5XHUwMDIwXHUwMDZjXHUwMDZmXHUwMDc2XHUwMDY1XHUwMDIwSmF2YVxu"), StandardCharsets.UTF_8);
        UnicodeEscapeYeeterCharIn a = new UnicodeEscapeYeeterCharIn(new ReaderCharIn(new StringReader(hmm)));
        StringBuilder o = new StringBuilder();
        int r;
        while ((r = a.read()) != -1) {
            o.append((char)r);
        }
        String result = o.toString();
        assertEquals("// I love Java\\n", result);
    }    
}
