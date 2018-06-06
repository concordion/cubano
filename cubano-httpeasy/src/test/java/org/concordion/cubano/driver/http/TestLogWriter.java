package org.concordion.cubano.driver.http;

public class TestLogWriter extends LogWriter {

    @Override
    public void info(String msg, Object... args) {
        System.out.println(getFormattedMessage(msg, args));
    }

    @Override
    public void request(String msg, Object... args) {
        System.out.println(getFormattedMessage(msg, args));
    }

    @Override
    public void response(String msg, Object... args) {
        System.out.println(getFormattedMessage(msg, args));
    }

    @Override
    public void error(String message, Throwable t) {
        System.out.println(getFormattedMessage(message));
    }

}

