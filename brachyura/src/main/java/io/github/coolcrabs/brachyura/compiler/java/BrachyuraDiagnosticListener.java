package io.github.coolcrabs.brachyura.compiler.java;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticListener;

import org.tinylog.Logger;

public enum BrachyuraDiagnosticListener implements DiagnosticListener {
    INSTANCE;

    @Override
    public void report(Diagnostic diagnostic) {
        switch (diagnostic.getKind()) {
            case ERROR:
                Logger.error(diagnostic.toString());
                break;
            case WARNING:
            case MANDATORY_WARNING:
                Logger.warn(diagnostic.toString());
                break;
            case NOTE:
            case OTHER:
            default:
                Logger.info(diagnostic.toString());
                break;
        }
    }
}
