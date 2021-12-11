package io.github.coolmineman.trieharder;

import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Maps old -> replacement strings
 * Prefers longer keys
 * 
 * Relevant: 
 * https://stackoverflow.com/questions/1326682/java-replacing-multiple-different-substring-in-a-string-at-once-or-in-the-most (Didn't use their impl but gave idea)
 * https://www.baeldung.com/trie-java
 */
public final class FastMultiSubstringReplacer {
    Trie trie;
    boolean ignoreComments;

    public FastMultiSubstringReplacer() {
        trie = new Trie();
    }

    public FastMultiSubstringReplacer(Map<String, String> replacements) {
        this();
        addReplacements(replacements);
    }

    public void addReplacements(Map<String, String> replacements) {
        for (Entry<String, String> entry : replacements.entrySet()) {
            trie.insert(entry.getKey(), entry.getValue());
        }
    }

    public void addReplacement(String a, String b) {
        trie.insert(a, b);
    }

    public void replace(Reader in, Appendable out) {
        replace(new ReaderCharIn(in), out);
    }

    public void replace(ReplacerCharIn in, Appendable out) {
        try {
            trie.doReplacement(in, out);
        } catch (IOException e) {
            throw Util.sneak(e);
        }
    }

    // Simple buffer
    static class ReaderBuffer {
        ReplacerCharIn reader;
        int[] buffer;
        int bufferPointer;
        int bufferSize;
        int mark;

        ReaderBuffer(ReplacerCharIn reader, int maxSize) {
            buffer = new int[maxSize];
            this.reader = reader;
        }

        int read() throws IOException {
            if (bufferSize > bufferPointer) {
                return buffer[bufferPointer++];
            } else {
                int r = reader.read();
                buffer[bufferPointer] = r;
                bufferPointer++;
                bufferSize++;
                return r;
            }
        }

        void mark() {
            mark = bufferPointer;
        }

        void reset() {
            bufferPointer = mark;
        }

        int pop() throws IOException {
            if (bufferSize > 0) {
                int r = buffer[0];
                System.arraycopy(buffer, 1, buffer, 0, buffer.length - 1);
                bufferSize--;
                bufferPointer = Math.max(0, bufferPointer - 1);
                return r;
            } else {
                return reader.read();
            }
        }

        void clear(int amount) throws IOException {
            for (int i = 0; i < amount; i++) pop(); //TODO optimize?
        }
    }
    
    static class Trie {
        TrieNode root = new TrieNode();
        int maxDepth = 0;

        void insert(String key, String value) {
            TrieNode[] current = new TrieNode[] {root}; // Dumb lambda rules
            
            char[] chars = key.toCharArray();
            for (int i = 0; i < chars.length; i++) {
                current[0] = current[0].children.computeIfAbsent(chars[i], c -> {
                    TrieNode n = new TrieNode();
                    n.parent = current[0];
                    return n;
                });
                current[0].depth = i + 1;
            }
            current[0].isWord = true;
            current[0].replacement = value;
            if (chars.length > maxDepth) maxDepth = chars.length;
        }

        void doReplacement(ReplacerCharIn in, Appendable out) throws IOException {
            ReaderBuffer in2 = new ReaderBuffer(in, maxDepth);
            while (true) {
                in2.mark();
                TrieNode current = root;
                int depth = 0;
                int read;
                boolean readChars = false;
                while ((read = in2.read()) != -1) {
                    readChars = true;
                    char c = (char) read;
                    TrieNode node = current.children.get((Character) c);
                    if (node == null) {
                        break;
                    }
                    current = node;
                    depth++;
                }
                if (!readChars) return;
                
                while (current != null && !current.isWord) {
                    current = current.parent;
                    depth--;
                }
                in2.reset();
                if (current == null) {
                    int o = in2.pop();
                    if (o >= 0) {
                        out.append((char)o);
                    }
                } else {
                    in2.clear(depth);
                    out.append(current.replacement);
                }
            }
        }
    }

    static class TrieNode {
        TrieNode parent = null;
        int depth = 0;
        HashMap<Character, TrieNode> children = new HashMap<>();
        String replacement = null;
        boolean isWord = false;
    }
}
