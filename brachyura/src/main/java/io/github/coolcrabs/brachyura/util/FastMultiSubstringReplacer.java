package io.github.coolcrabs.brachyura.util;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
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
public class FastMultiSubstringReplacer {
    Trie trie;

    public FastMultiSubstringReplacer(Map<String, String> replacements) {
        trie = new Trie();
        for (Entry<String, String> entry : replacements.entrySet()) {
            trie.insert(entry.getKey(), entry.getValue());
        }
    }

    public void replace(Reader in, Writer out) {
        try {
            trie.doReplacement(in, out);
        } catch (IOException e) {
            throw Util.sneak(e);
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

        void doReplacement(Reader in, Writer out) throws IOException {
            while (true) {
                in.mark(maxDepth);
                TrieNode current = root;
                int read;
                boolean readChars = false;
                while ((read = in.read()) != -1) {
                    readChars = true;
                    char c = (char) read;
                    TrieNode node = current.children.get((Character) c);
                    if (node == null) {
                        break;
                    }
                    current = node;
                }
                if (!readChars) return;
                while (current != null && !current.isWord) {
                    current = current.parent;
                }
                in.reset();
                if (current == null) {
                    out.write(in.read());
                } else {
                    in.skip(current.depth);
                    out.write(current.replacement);
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
