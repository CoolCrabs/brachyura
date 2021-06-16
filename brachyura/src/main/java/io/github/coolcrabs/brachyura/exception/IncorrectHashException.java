package io.github.coolcrabs.brachyura.exception;

public class IncorrectHashException extends RuntimeException {
    public IncorrectHashException(String expected, String got) {
        super("Expected " + expected + " Got " + got);
    }
}
