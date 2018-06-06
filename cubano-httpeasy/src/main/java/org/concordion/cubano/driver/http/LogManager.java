package org.concordion.cubano.driver.http;

public class LogManager {
    private LogWriter logWriter;
    private boolean logRequestDetails = false;
    
    public LogManager(LogWriter logWriter, boolean logRequestDetails) {
        // If not provided, revert to default setting
        if (logWriter == null) {
            this.logWriter = HttpEasyDefaults.getDefaultLogWriter();
        } else {
            this.logWriter = logWriter;
        }

        if (!logRequestDetails) {
            this.logRequestDetails = HttpEasyDefaults.getLogRequestDetails();
        } else {
            this.logRequestDetails = logRequestDetails;
        }
    }

    public boolean isLogRequestDetails() {
        return logRequestDetails;
    }

    public void info(String msg, Object... args) {
        logWriter.info(msg, args);
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
