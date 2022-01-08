package io.github.coolcrabs.fernutil;

import org.jetbrains.java.decompiler.main.extern.IFernflowerLogger;
import org.tinylog.Logger;

class TFFLogger extends IFernflowerLogger {

    @Override
    public void writeMessage(String message, Severity severity) {
        switch (severity) {
            case WARN:
                Logger.warn(message);
                break;
            case ERROR:
                Logger.error(message);
                break;
            case TRACE:
            case INFO:
            default:
                Logger.info(message);
                break;
        }
    }

    @Override
    public void writeMessage(String message, Severity severity, Throwable t) {
        switch (severity) {
            case WARN:
                Logger.warn(message);
                Logger.warn(t);
                break;
            case ERROR:
                Logger.error(message);
                Logger.error(t);
                break;
            case TRACE:
            case INFO:
            default:
                Logger.info(message);
                Logger.info(t);
                break;
        }
    }

}
