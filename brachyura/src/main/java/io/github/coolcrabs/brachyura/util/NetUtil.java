package io.github.coolcrabs.brachyura.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
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

    public static URI uri(String uri) {
        try {
            return new URI(uri);
        } catch (URISyntaxException e) {
            throw Util.sneak(e);
        }
    }

    public static InputStream inputStream(URL url) {
        try {
            return url.openStream();
        } catch (IOException e) {
            throw Util.sneak(e);
        }
    }
}
