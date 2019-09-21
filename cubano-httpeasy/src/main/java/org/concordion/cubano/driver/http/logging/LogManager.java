package org.concordion.cubano.driver.http.logging;

import org.concordion.cubano.driver.http.HttpEasyDefaults;

public class LogManager {
    private boolean logRequest;
    private boolean logRequestDetails;

    private LogWriter logWriter;
    private LogBuffer logBuffer = null;

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

    public void error(String message, Throwable t) {
        logWriter.error(message, t);
    }

    public void flushInfo() {
        if (this.logRequest && logBuffer != null) {
            if (logBuffer.length() > 0) {
                logBuffer.trimNewLine();

                String[] lines = logBuffer.toString().split("\\r?\\n");

                for (String line : lines) {
                    logWriter.info(line);
                }
            }
        }

        logBuffer = null;
    }

    public void flushRequest() {
        if (this.logRequestDetails && logBuffer != null) {
            if (logBuffer.length() > 0) {
                logBuffer.trimNewLine();
                logWriter.request(logBuffer.toString());
            }
        }

        logBuffer = null;
    }

    public void flushResponse() {
        if (this.logRequestDetails && logBuffer != null) {
            if (logBuffer.length() > 0) {
                logBuffer.trimNewLine();
                logWriter.response(logBuffer.toString());
            }
        }

        logBuffer = null;
    }

    public LogBuffer getBuffer() {
        if (this.logBuffer == null) {
            this.logBuffer = new LogBuffer();
        }

        return this.logBuffer;
    }
}