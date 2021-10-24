package io.github.coolcrabs.brachyura.profiler;

import java.lang.management.ManagementFactory;
import java.nio.file.Path;
import java.util.HashMap;

import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.tinylog.Logger;

import io.github.coolcrabs.brachyura.plugins.Plugin;
import io.github.coolcrabs.brachyura.util.PathUtil;
import io.github.coolcrabs.brachyura.util.Util;

// https://github.com/jeanbisutti/quick-perf-connection-profiling/blob/main/jvm/jfr-annotations/src/main/java/org/quickperf/jvm/jfr/profiler/JdkFlightRecorderProfilerFromJava9.java
// https://docs.oracle.com/javase/9/docs/api/jdk/management/jfr/FlightRecorderMXBean.html
// In java 8 but not api...

public enum ProfilePlugin implements Plugin {
    INSTANCE;

    static final boolean profile = Boolean.getBoolean("profile");

    boolean init = false;
    boolean usable = false;
    MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
    ObjectName objectName = null;
    HashMap<Long, String> path = new HashMap<>();

    Long currentRecord;

    @Override
    public void onEntry() {
        if (profile && init()) {
            currentRecord = startRecording(PathUtil.CWD.resolve(System.currentTimeMillis() + ".jfr"));
        }
    }

    @Override
    public void onExit() {
        if (profile && init()) {
            stopRecording(currentRecord);
        }
    }

    Long startRecording(Path file) {
        try {
            Long recordingId = (Long) mBeanServer.invoke(objectName, "newRecording", new Object[]{}, new String[0]);

            Object[] setConfigArgs = new Object[]{recordingId, "profile"};
            mBeanServer.invoke(objectName, "setPredefinedConfiguration", setConfigArgs, new String[]{long.class.getName(), String.class.getName()});

            path.put(recordingId, file.toString());

            // I was going to do the following to set the destination value
            // But that doesn't work in java 8 since they left the option out of the impl for some reason
            // This code is too scuffed to delete, enjoy.

            // String typeName = "java.util.Map<java.lang.String, java.lang.String>";
            // String[] keyValue = new String[] {"key", "value"};
            // OpenType[] openTypes = new OpenType[] {SimpleType.STRING, SimpleType.STRING};
            // CompositeType rowType = new CompositeType(typeName, typeName, keyValue, keyValue, openTypes);
            // TabularType tabularType = new TabularType(typeName, typeName, rowType, new String[] {"key"});
            // TabularDataSupport tabularData = new TabularDataSupport(tabularType);
            // tabularData.put(new CompositeDataSupport(rowType, new String[] {"key", "value"}, new String[] {"destination", file.toString()}));
            // Object[] setRecordingOptions​Args = new Object[] {recordingId, tabularData};
            // mBeanServer.invoke(objectName, "setRecordingOptions", setRecordingOptions​Args, new String[]{long.class.getName(), TabularData.class.getName()});

            Object[] startRecordingArgs = new Object[]{recordingId};
            mBeanServer.invoke(objectName, "startRecording", startRecordingArgs, new String[]{long.class.getName()});
            return recordingId;
        } catch (Exception e) {
            throw Util.sneak(e);
        }
    }

    void stopRecording(Long id) {
        try {
            Object[] stopRecordingArgs = new Object[] {id};
            mBeanServer.invoke(objectName, "stopRecording", stopRecordingArgs, new String[]{long.class.getName()});

            Object[] copyToArgs = new Object[] {id, null};
            path.computeIfPresent(id, (k, v) -> {
                copyToArgs[1] = v;
                return null;
            });
            mBeanServer.invoke(objectName, "copyTo", copyToArgs, new String[]{long.class.getName(), String.class.getName()});
            Logger.info("Saved jfr recording: " + copyToArgs[1]);

            Object[] closeRecordingArgs = new Object[] {id};
            mBeanServer.invoke(objectName, "closeRecording", closeRecordingArgs, new String[]{long.class.getName()});
        } catch (Exception e) {
            throw Util.sneak(e);
        }
    }

    boolean init() {
        if (!init) {
            init = true;
            try {
                objectName = new ObjectName("jdk.management.jfr:type=FlightRecorder");
                if (mBeanServer.isRegistered(objectName)) {
                    return true;
                } else {
                    Logger.warn("JFR not found");
                    return false;
                }
            } catch (MalformedObjectNameException e) {
                Logger.warn("Unable to create jfr name:");
                Logger.warn(e);
                return false;
            }
        } else {
            return usable;
        }
    }

}
