package io.github.coolcrabs.brachyura.decompiler;

public class LineNumberTableEntry {
    public final short startPc;
    public final short lineNumber;

    public LineNumberTableEntry(short startPc, short lineNumber) {
        this.startPc = startPc;
        this.lineNumber = lineNumber;
    }

    public LineNumberTableEntry(int startPc, int lineNumber) {
        this.startPc = (short) startPc;
        this.lineNumber = (short) lineNumber;
    }
}
