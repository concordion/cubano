package org.concordion.cubano.driver.http;

public class TestLogWriter implements LogWriter {

	@Override
	public void info(String message) {
		System.out.println(message);
	}

}

