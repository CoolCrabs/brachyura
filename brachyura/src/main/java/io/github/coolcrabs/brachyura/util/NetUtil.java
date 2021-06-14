package io.github.coolcrabs.brachyura.util;

import java.net.MalformedURLException;
import java.net.URL;

public class NetUtil {
    private NetUtil() { }

    public static URL url(String url) {
        try {
            return new URL(url);
        } catch (MalformedURLException e) {
            throw Util.sneak(e);
        }
    }
}
