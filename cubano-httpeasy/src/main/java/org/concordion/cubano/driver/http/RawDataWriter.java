package org.concordion.cubano.driver.http;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import com.google.common.net.MediaType;

/**
 * Attach a File or data to an http request.
 *
 * @author Andrew Sumner
 */
class RawDataWriter implements DataWriter {
    private HttpURLConnection connection;
    private byte[] postEndcoded = null;
    private File uploadFile = null;
    private InputStream uploadStream = null;
    private String uploadFileName;

    /**
     * Constructor.
     *
     * @param connection       The connection
     * @param rawData          data (File or String)
     * @param rawDataMediaType Type of attachment
     * @param fileName         file name for InputStream
     * @throws UnsupportedEncodingException
     */
    public RawDataWriter(HttpURLConnection connection, Object rawData, MediaType rawDataMediaType, String fileName) {
        this.connection = connection;

        if (rawData instanceof File) {
            uploadFile = (File) rawData;
            connection.setRequestProperty("Content-Type", rawDataMediaType.toString());
            connection.setRequestProperty("Content-Length", Long.toString(uploadFile.length()));

        } else if (rawData instanceof InputStream) {
            uploadStream = (InputStream) rawData;
            uploadFileName = fileName;

            connection.setRequestProperty("Content-Type", rawDataMediaType.toString());
        } else {
            // Assume data is encoded correctly
            //this.postEndcoded = rawData.getBytes(URLEncoder.encode(String.valueOf(data), "UTF-8"));
            this.postEndcoded = String.valueOf(rawData).getBytes(StandardCharsets.UTF_8);

            connection.setRequestProperty("charset", "utf-8");
            connection.setRequestProperty("Content-Type", rawDataMediaType.toString());
            connection.setRequestProperty("Content-Length", Integer.toString(postEndcoded.length));
        }
    }

    @Override
    public void write(LogManager logger) throws IOException {
        StringBuilder logBuffer = new StringBuilder();

        if (uploadFile != null) {
            logBuffer.append("  With content of file: ").append(uploadFile.getAbsolutePath());

            try (FileInputStream inputStream = new FileInputStream(uploadFile)) {
                write(inputStream);
            }

        } else if (uploadStream != null) {
            logBuffer.append("  With content of file: ").append(uploadFileName);

            long length = write(uploadStream);
            connection.setRequestProperty("Content-Length", Long.toString(length));

        } else {
            logBuffer.append("  With content: ").append(Arrays.toString(postEndcoded));

            try (DataOutputStream wr = new DataOutputStream(connection.getOutputStream())) {
                wr.write(postEndcoded);
            }
        }

        logger.info(logBuffer.toString());
    }

    private long write(InputStream inputStream) throws IOException {
        long length = 0;
        byte[] buffer = new byte[4096];
        int bytesRead = -1;

        OutputStream outputStream = connection.getOutputStream();

        while ((bytesRead = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, bytesRead);
            length += bytesRead;
        }

        outputStream.flush();

        return length;
    }

}
