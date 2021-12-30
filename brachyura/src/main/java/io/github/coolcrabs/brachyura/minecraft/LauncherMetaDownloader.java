package io.github.coolcrabs.brachyura.minecraft;

import java.io.InputStreamReader;

import com.google.gson.Gson;

import io.github.coolcrabs.brachyura.util.NetUtil;
import io.github.coolcrabs.brachyura.util.Util;

class LauncherMetaDownloader {
    private LauncherMetaDownloader() { }

    private static LauncherMeta meta;

    public static LauncherMeta getLauncherMeta() {
        if (meta == null) {
            try {
                meta = (new Gson()).fromJson(new InputStreamReader(NetUtil.inputStream(NetUtil.url("https://launchermeta.mojang.com/mc/game/version_manifest.json"))), LauncherMeta.class);
            } catch (Exception e) {
                throw Util.sneak(e);
            }
        }
        return meta;
    }
}
