package org.concordion.cubano.driver.http;

public class TestLogWriter implements LogWriter {

	String message = "";
	// private final ReportLogger log =
	// ReportLoggerFactory.getReportLogger(ToStoryBoardLogger.class);

	@Override
	public void info(String message) {
		System.out.println(message);
		this.message = message;
        // log.with().message(" Message for Message")
        // .attachment(message, "Message", MediaType.JSON)
		// .marker(StoryboardMarkerFactory.addCard(message, StockCardImage.JSON,
		// CardResult.SUCCESS))
		// .debug();

	}

}

