package org.concordion.cubano.driver.http;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.slf4j.Logger;

/**
 * Attach an "application/x-www-form-urlencoded" form to an http request.
 *
 * @author Andrew Sumner
 */
class FormUrlEncodedDataWriter implements DataWriter {
    private final HttpURLConnection connection;
    private final byte[] postEndcoded;

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
            postData.append(URLEncoder.encode(String.valueOf(field.value), "UTF-8"));
        }

        postEndcoded = postData.toString().getBytes(StandardCharsets.UTF_8);

        connection.setRequestProperty("charset", "utf-8");
        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        connection.setRequestProperty("Content-Length", Integer.toString(postEndcoded.length));
    }

    @Override
    public void write(Logger logger) throws IOException {
        if (logger != null) {
            logger.trace("With Content:{}\t{}", System.lineSeparator(), new String(postEndcoded, "UTF-8"));
        }

        try (DataOutputStream wr = new DataOutputStream(connection.getOutputStream())) {
            wr.write(postEndcoded);
        }
    }

}
