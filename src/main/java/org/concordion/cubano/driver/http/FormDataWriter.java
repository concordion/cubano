package org.concordion.cubano.driver.http;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.slf4j.Logger;

import com.google.common.net.MediaType;

/**
 * Attach a "multipart/form-data" form to an http request.
 *
 * @author Andrew Sumner
 */
class FormDataWriter implements DataWriter {
    private final HttpURLConnection connection;
    private final List<Field> fields;
    private final String boundary = "FormBoundary" + System.currentTimeMillis();
    private OutputStream outputStream;
    private PrintWriter writer = null;
    private static final String LINE_FEED = "\r\n";
    private StringBuilder logBuffer = null;

    /**
     * Constructor.
     *
     * @param connection The connection
     * @param query      Query string
     * @param fields     Fields to write to form
     * @throws UnsupportedEncodingException
     */
    public FormDataWriter(HttpURLConnection connection, String query, List<Field> fields) throws UnsupportedEncodingException {
        this.connection = connection;
        this.fields = fields;

        connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
    }

    @Override
    public void write(Logger logger) throws IOException {
        outputStream = connection.getOutputStream();

        if (logger != null) {
            logBuffer = new StringBuilder();
        }

        try {
            writer = new PrintWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8), true);

            for (Field field : fields) {
                if (field.value instanceof File) {
                    addFilePart(field.name, (File) field.value, field.type);
                } else if (field.value instanceof InputStream) {
                    addFilePart(field.name, (InputStream) field.value, field.type, field.fileName);
                } else {
                    addFormField(field.name, field.value);
                }
            }

            writeFinalBoundary();
        } finally {
            if (logger != null) {
                logger.trace("With Content:{}\t{}", LINE_FEED, logBuffer.toString().replace(LINE_FEED, LINE_FEED + "\t"));
            }

            if (writer != null) {
                writer.close();
            }
        }
    }

    private void writeFieldBoundary() {
        StringBuilder buf = new StringBuilder();
        buf.append("--").append(boundary).append(LINE_FEED);

        if (logBuffer != null) {
            logBuffer.append(buf);
        }

        writer.append(buf);
    }

    private void writeFinalBoundary() {
        StringBuilder buf = new StringBuilder();
        buf.append("--").append(boundary).append("--").append(LINE_FEED);

        if (logBuffer != null) {
            logBuffer.append(buf);
        }

        writer.append(buf);
    }

    /**
     * Adds a form field to the request.
     *
     * @param name  field name
     * @param value field value
     */
    private void addFormField(String name, Object value) {
        StringBuilder buf = new StringBuilder();

        writeFieldBoundary();
        buf.append("Content-Disposition: form-data; name=\"").append(name).append("\"").append(LINE_FEED);
        // buf.append("Content-Type: text/plain; charset=utf-8").append(LINE_FEED);
        buf.append(LINE_FEED);
        buf.append(String.valueOf(value)).append(LINE_FEED);

        if (logBuffer != null) {
            logBuffer.append(buf);
        }

        writer.append(buf);
        writer.flush();
    }

    /**
     * Adds a upload file section to the request.
     *
     * @param fieldName  name attribute in <input type="file" name="..." />
     * @param uploadFile a File to be uploaded
     * @param type       MediaType of the file
     * @throws IOException
     */
    private void addFilePart(String fieldName, File uploadFile, MediaType type) throws IOException {
        try (FileInputStream inputStream = new FileInputStream(uploadFile)) {
            addFilePart(fieldName, inputStream, type, uploadFile.getName());
        }
    }

    /**
     * Adds a upload stream section to the request.
     *
     * @param fieldName   name attribute in <input type="file" name="..." />
     * @param inputStream a File to be uploaded
     * @param type        MediaType of the file
     * @param fileName    name of file to be uploaded
     * @throws IOException
     */
    private void addFilePart(String fieldName, InputStream inputStream, MediaType type, String fileName) throws IOException {
        StringBuilder buf = new StringBuilder();

        writeFieldBoundary();
        buf.append("Content-Disposition: form-data; name=\"").append(fieldName).append("\"; filename=\"").append(fileName).append("\"").append(LINE_FEED);
        if (type == null) {
            buf.append("Content-Type: ").append(URLConnection.guessContentTypeFromName(fileName)).append(LINE_FEED);
        } else {
            buf.append("Content-Type: ").append(type.toString()).append(LINE_FEED);
        }

        // buf.append("Content-Transfer-Encoding: binary").append(LINE_FEED);
        buf.append(LINE_FEED);

        if (logBuffer != null) {
            logBuffer.append(buf);
            logBuffer.append("... Content of file ").append(fileName).append(" ...").append(LINE_FEED);
        }

        writer.append(buf);
        writer.flush();

        byte[] buffer = new byte[4096];
        int bytesRead = -1;
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, bytesRead);
        }
        outputStream.flush();

        writer.append(LINE_FEED);
        writer.flush();
    }
}
