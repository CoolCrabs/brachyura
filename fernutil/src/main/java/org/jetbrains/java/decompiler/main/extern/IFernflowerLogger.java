package org.jetbrains.java.decompiler.main.extern;

public abstract class IFernflowerLogger {

    public enum Severity {
        TRACE("TRACE: "), INFO("INFO:  "), WARN("WARN:  "), ERROR("ERROR: ");

        public final String prefix;

        Severity(String prefix) {
            this.prefix = prefix;
        }
    }

    private Severity severity = Severity.INFO;

    public boolean accepts(Severity severity) {
        return severity.ordinal() >= this.severity.ordinal();
    }

    public void setSeverity(Severity severity) {
        this.severity = severity;
    }

    public abstract void writeMessage(String message, Severity severity);

    public abstract void writeMessage(String message, Severity severity, Throwable t);

    public void writeMessage(String message, Throwable t) {
        writeMessage(message, Severity.ERROR, t);
    }

    public void startReadingClass(String className) {
    }

    public void endReadingClass() {
    }

    public void startClass(String className) {
    }

    public void endClass() {
    }

    public void startMethod(String methodName) {
    }

    public void endMethod() {
    }

    public void startWriteClass(String className) {
    }

    public void endWriteClass() {
    }
}