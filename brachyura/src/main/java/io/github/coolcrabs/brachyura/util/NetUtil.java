package io.github.coolcrabs.brachyura.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.Base64;
import java.util.zip.GZIPInputStream;
import org.jetbrains.annotations.Nullable;
import org.tinylog.Logger;

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
            URLConnection con = url.openConnection();
            con.addRequestProperty("Accept-Encoding", "gzip");
            long size = con.getContentLengthLong();
            Logger.info("Downloading {} ({})", size == -1 ? "unknown size" : humanReadableByteCountSI(size));
            if ("gzip".equals(con.getContentEncoding())) {
                return new GZIPInputStream(con.getInputStream());
            } else {
                return con.getInputStream();
            }
        } catch (IOException e) {
            throw Util.sneak(e);
        }
    }
    
    // https://stackoverflow.com/a/3758880
    public static String humanReadableByteCountSI(long bytes) {
        if (-1000 < bytes && bytes < 1000) {
            return bytes + " B";
        }
        CharacterIterator ci = new StringCharacterIterator("kMGTPE");
        while (bytes <= -999_950 || bytes >= 999_950) {
            bytes /= 1000;
            ci.next();
        }
        return String.format("%.1f %cB", bytes / 1000.0, ci.current());
    }
    
    // https://gist.github.com/luankevinferreira/5221ea62e874a9b29d86b13a2637517b
    // https://stackoverflow.com/a/3283496
    public static void put(URL url, InputStream is, @Nullable String username, @Nullable String password) {
        try {
            Logger.info("Uploading to {}...", url);
            URLConnection con = url.openConnection();
            con.setDoInput(true);
            con.setDoOutput(true);
            if (con instanceof HttpURLConnection) {
               ((HttpURLConnection)con).setRequestMethod("PUT"); 
            }
            con.setRequestProperty("Content-type", "application/octet-stream");
            // https://stackoverflow.com/a/3283496
            if (username != null && password != null) {
                con.setRequestProperty("Authorization", "Basic " + Base64.getEncoder().encodeToString((username + ":" + password).getBytes(StandardCharsets.UTF_8)));
            }
            con.connect();
            try (OutputStream os = con.getOutputStream()) {
                StreamUtil.copy(is, os);
            }
            if (con instanceof HttpURLConnection) {
                Logger.info(((HttpURLConnection)con).getResponseMessage());
            }
        } catch (IOException ex) {
            throw Util.sneak(ex);
        }
    }
}
