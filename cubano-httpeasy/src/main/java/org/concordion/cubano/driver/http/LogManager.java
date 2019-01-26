package org.concordion.cubano.driver.http;

public class LogManager {
    private LogWriter logWriter;
    private boolean logRequest;
    private boolean logRequestDetails;
    private StringBuilder buffer = new StringBuilder();
    
    public static final String NEW_LINE = System.lineSeparator();
    public static final String TAB = "\t";

    public LogManager(LogWriter logWriter, boolean logRequestDetails) {
        this.logRequest = HttpEasyDefaults.getLogRequest() || logRequestDetails;
        this.logRequestDetails = logRequestDetails;

        this.logWriter = logWriter;
    }

    public boolean isLogRequestDetails() {
        return logRequestDetails;
    }

    public void info(String msg, Object... args) {
        if (logRequest) {
            logWriter.info(msg, args);
        }
    }

    public LogManager buffer(String str) {
        buffer.append(str);
        return this;
    }

    public LogManager bufferLine(String str) {
        buffer.append(str).append(NEW_LINE);
        return this;
    }

    public LogManager bufferIndented(String str) {
        buffer.append(TAB).append(str);
        return this;
    }

    public LogManager bufferIndentedLine(String str) {
        buffer.append(TAB).append(str);

        if (!bufferEndsWith(NEW_LINE)) {
            buffer.append(NEW_LINE);
        }

        return this;
    }

    public LogManager bufferIndentedLines(String str) {
        buffer.append(TAB).append(str.replace(NEW_LINE, NEW_LINE + TAB));

        if (!bufferEndsWith(NEW_LINE + TAB)) {
            buffer.append(NEW_LINE);
        }

        return this;
    }

    public void flushRequest() {
        if (logRequestDetails && buffer.length() > 0) {
            trimNewLine();
            logWriter.request(buffer.toString());
        }

        buffer = new StringBuilder();
    }

    public void flushResponse() {
        if (logRequestDetails && buffer.length() > 0) {
            trimNewLine();
            logWriter.response(buffer.toString());
        }

        buffer = new StringBuilder();
    }

    private void trimNewLine() {
        if (bufferEndsWith(NEW_LINE)) {
            buffer.setLength(buffer.length() - NEW_LINE.length());
        }
    }

    private boolean bufferEndsWith(String str) {
        return buffer.substring(buffer.length() - str.length()).equals(str);
    }

    public void error(String message, Throwable t) {
        logWriter.error(message, t);
    }
}
