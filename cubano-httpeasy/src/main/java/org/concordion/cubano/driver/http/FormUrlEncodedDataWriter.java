package org.concordion.cubano.driver.http;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Attach an "application/x-www-form-urlencoded" form to an http request.
 *
 * @author Andrew Sumner
 */
class FormUrlEncodedDataWriter implements DataWriter {
    private final HttpURLConnection connection;
    private final byte[] postEndcoded;
    private static final String NEW_LINE = System.lineSeparator();
    private static final String TAB = "\t";

    /**
     * Constructor.
     *
     * @param connection The connection
     * @param query      Query string
     * @param fields     Fields to write to form
     * @throws UnsupportedEncodingException
     */
    public FormUrlEncodedDataWriter(HttpURLConnection connection, String query, List<Field> fields) throws UnsupportedEncodingException {
        this.connection = connection;

        StringBuilder postData = new StringBuilder();

        if (query != null && !query.isEmpty()) {
            postData.append(query);
        }

        for (Field field : fields) {
            if (postData.length() > 0) {
                postData.append('&');
            }
            postData.append(field.name);
            postData.append('=');
            postData.append(URLEncoder.encode(String.valueOf(field.value), StandardCharsets.UTF_8.name()));
        }

        postEndcoded = postData.toString().getBytes(StandardCharsets.UTF_8);

        connection.setRequestProperty("charset", StandardCharsets.UTF_8.name());
        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        connection.setRequestProperty("Content-Length", Integer.toString(postEndcoded.length));
    }

    @Override
    public void write(LogManager logger) throws IOException {
        String logparams = new String(postEndcoded, StandardCharsets.UTF_8);

        for (String key : HttpEasyDefaults.getSensitiveParameters()) {
            logparams = logparams.replaceFirst("(?i)(?<=\\?|&|^)" + key + "=.*?(?=$|&)", key + "=xxx");
        }

        logger.buffer("Request Content:{}application/x-www-form-urlencoded:{}{}", NEW_LINE + TAB, NEW_LINE + TAB, logparams.replace(NEW_LINE, NEW_LINE + TAB));

        try (DataOutputStream wr = new DataOutputStream(connection.getOutputStream())) {
            wr.write(postEndcoded);
        }
    }

}
