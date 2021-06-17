package io.github.coolcrabs.brachyura.minecraft;

class LauncherMeta {
    public Latest latest;
    public Version[] versions;

    public class Latest {
        public String release;
        public String snapshot;
    }

    public class Version {
        public String id;
        public String type;
        public String url;
        public String time;
        public String releaseTime;
    }
}
