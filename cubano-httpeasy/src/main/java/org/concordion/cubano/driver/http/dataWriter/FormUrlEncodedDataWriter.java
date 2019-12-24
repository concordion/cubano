package org.concordion.cubano.driver.http.dataWriter;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.concordion.cubano.driver.http.HttpEasyDefaults;
import org.concordion.cubano.driver.http.logging.LogManager;

/**
 * Attach an "application/x-www-form-urlencoded" form to an http request.
 *
 * @author Andrew Sumner
 */
public class FormUrlEncodedDataWriter implements DataWriter {
    private final HttpURLConnection connection;
    private final byte[] postEndcoded;

    /**
     * Constructor.
     *
     * @param connection The connection
     * @param query Query string
     * @param fields Fields to write to form
     * @throws UnsupportedEncodingException The character encoding is not supported
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
            logparams = logparams.replaceFirst("(?i)(?<=\\?|&|^)" + key + "=.*?(?=$|&)", key + "=*****");
        }

        if (logger.isLogRequestDetails()) {
            logger.getBuffer().writeLine("Request Content (application/x-www-form-urlencoded):");
        } else {
            logger.getBuffer().setIndentLevel(1).writeLine("With application/x-www-form-urlencoded content:");
        }
        logger.getBuffer().writeIndentedLine(logparams);

        try (DataOutputStream wr = new DataOutputStream(connection.getOutputStream())) {
            wr.write(postEndcoded);
        }
    }

}
