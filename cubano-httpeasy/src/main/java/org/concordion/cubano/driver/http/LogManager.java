package org.concordion.cubano.driver.http;

public class LogManager {
    private LogWriter logWriter;
    private boolean logRequest;
    private boolean logRequestDetails;
    private StringBuilder buffer = new StringBuilder();
    
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

    public void buffer(String msg, Object... args) {
        buffer.append(logWriter.getFormattedMessage(msg, args));
    }

    public void flushRequest() {
        if (logRequestDetails && buffer.length() > 0) {
            logWriter.request(buffer.toString());
        }

        buffer = new StringBuilder();
    }

    public void request(String msg, Object... args) {
        if (logRequestDetails) {
            logWriter.request(msg, args);
        }
    }
    
    public void flushResponse() {
        if (logRequestDetails && buffer.length() > 0) {
            logWriter.response(buffer.toString());
        }

        buffer = new StringBuilder();
    }

    public void response(String msg, Object... args) {
        if (logRequestDetails) {
            logWriter.response(msg, args);
        }
    }

    public void error(String message, Throwable t) {
        logWriter.error(message, t);
    }
}
