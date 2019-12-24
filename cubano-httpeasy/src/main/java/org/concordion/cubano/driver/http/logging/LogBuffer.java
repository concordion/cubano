package org.concordion.cubano.driver.http.logging;

public class LogBuffer {
    private static final String NEW_LINE = System.lineSeparator();
    private static final String TAB = "  ";

    private StringBuilder buffer = new StringBuilder();
    private int indentLevel = 0;

    public LogBuffer setIndentLevel(int level) {
        this.indentLevel = level;

        return this;
    }

    private void indent() {
        if (buffer.length() == 0 || bufferEndsWith(NEW_LINE)) {
            for (int i = 0; i < this.indentLevel; i++) {
                buffer.append(TAB);
            }
        }
    }

    public LogBuffer write(String str) {
        indent();
        buffer.append(str);
        return this;
    }

    public LogBuffer writeLine(String str) {
        indent();
        buffer.append(str).append(NEW_LINE);
        return this;
    }

    public LogBuffer writeIndented(String str) {
        indent();
        buffer.append(TAB).append(str);

        return this;
    }

    public LogBuffer writeIndentedLine(String str) {
        indent();
        buffer.append(TAB).append(str);

        if (!bufferEndsWith(NEW_LINE)) {
            buffer.append(NEW_LINE);
        }

        return this;
    }

    public LogBuffer writeIndentedLines(String str) {
        String indent = TAB;

        for (int i = 0; i < this.indentLevel; i++) {
            indent += TAB;
        }

        buffer.append(indent).append(str.replace(NEW_LINE, NEW_LINE + indent));

        if (!bufferEndsWith(NEW_LINE + indent)) {
            buffer.append(NEW_LINE);
        }

        return this;
    }

    void trimNewLine() {
        if (bufferEndsWith(NEW_LINE)) {
            buffer.setLength(buffer.length() - NEW_LINE.length());
        }
    }

    boolean bufferEndsWith(String str) {
        return buffer.substring(buffer.length() - str.length()).equals(str);
    }

    public int length() {
        return buffer.length();
    }

    public String toString() {
        return buffer.toString();
    }
}