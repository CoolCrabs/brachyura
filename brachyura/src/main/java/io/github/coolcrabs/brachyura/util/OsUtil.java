package io.github.coolcrabs.brachyura.util;

import java.util.HashMap;
import java.util.Locale;

import io.github.coolcrabs.brachyura.exception.UnknownOsException;

public class OsUtil {
    private OsUtil() { }

    public static final Os OS;
    public static final String OS_VERSION = System.getProperty("os.version");

    private static final HashMap<String, Os> osMap = new HashMap<>();

    public enum Os {
        LINUX("linux"),
        OSX("osx"),
        WINDOWS("windows");

        public final String mojang;
        
        private Os(String mojang) {
            this.mojang = mojang;
            osMap.put(mojang, this);
        }

        public static Os fromMojang(String mojang) {
            return osMap.get(mojang);
        }
    }

    // https://stackoverflow.com/a/18417382
    static {
        String osString = System.getProperty("os.name", "generic").toLowerCase(Locale.ENGLISH);
        if ((osString.indexOf("mac") >= 0) || (osString.indexOf("darwin") >= 0)) {
            OS = Os.OSX;
        } else if (osString.indexOf("win") >= 0) {
            OS = Os.WINDOWS;
        } else if (osString.indexOf("nux") >= 0) {
            OS = Os.LINUX;
        } else {
            throw new UnknownOsException(); // Minecraft requires natives so knowing the os is required
        }
    }
}
