package io.github.coolcrabs.brachyura.minecraft;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.jetbrains.annotations.Nullable;

import io.github.coolcrabs.brachyura.exception.UnknownJsonException;
import io.github.coolcrabs.brachyura.util.OsUtil;

class Rules {
    private Rules() { }

    public static boolean allowed(@Nullable JsonElement element) {
        if (element == null) return true;
        if (!element.isJsonArray()) throw new UnknownJsonException("rules expected to be array or null");
        JsonArray array = element.getAsJsonArray();
        for (int i = array.size() - 1; i >= 0; i--) {
            JsonObject rule = array.get(i).getAsJsonObject();
            boolean action = action(rule.get("action").getAsString());
            JsonElement os = rule.get("os");
            if (os == null) {
                return action;
            } else {
                JsonObject os2 = os.getAsJsonObject();
                JsonElement name = os2.get("name");
                JsonElement version = os2.get("version");
                if ((name == null || OsUtil.OS == OsUtil.Os.fromMojang(name.getAsString())) && (version == null || OsUtil.OS_VERSION.matches(version.getAsString()))) {
                    return action;
                }
            }
        }
        return false;
    }

    private static boolean action(String action) {
        switch (action) {
        case "allow":
            return true;
        case "disallow":
            return false;
        default:
            throw new UnknownJsonException("action expected to be allow or disallow, got " + action);
        }
    }
}
