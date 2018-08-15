package org.concordion.cubano.driver.http;

public class LogManager {
    private LogWriter logWriter;
    private boolean logRequest;
    private boolean logRequestDetails;
    
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

    public void request(String msg, Object... args) {
        if (logRequestDetails) {
            logWriter.request(msg, args);
        }
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
