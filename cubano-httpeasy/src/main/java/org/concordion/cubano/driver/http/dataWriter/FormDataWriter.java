package org.concordion.cubano.driver.http.dataWriter;

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

import org.concordion.cubano.driver.http.HttpEasyDefaults;
import org.concordion.cubano.driver.http.logging.LogManager;

import com.google.common.net.MediaType;

/**
 * Attach a "multipart/form-data" form to an http request.
 *
 * @author Andrew Sumner
 */
public class FormDataWriter implements DataWriter {
    private final HttpURLConnection connection;
    private final List<Field> fields;
    private final String boundary = "FormBoundary" + System.currentTimeMillis();
    private OutputStream outputStream;
    private PrintWriter writer = null;
    private LogManager logger = null;
    private static final String NEW_LINE = System.lineSeparator();

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
    public void write(LogManager logger) throws IOException {
        this.logger = logger;
        outputStream = connection.getOutputStream();

        try {
            writer = new PrintWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8), true);

            if (logger.isLogRequestDetails()) {
                logger.getBuffer().writeLine("Request Content (multipart/form-data):");
            } else {
                logger.getBuffer().setIndentLevel(1).writeLine("With multipart/form-data content:");
            }

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
            if (writer != null) {
                writer.close();
            }
        }
    }

    private void writeFieldBoundary() {
        StringBuilder buf = new StringBuilder();
        buf.append("--").append(boundary).append(NEW_LINE);

        logger.getBuffer().writeIndented(buf.toString());

        writer.append(buf);
    }

    private void writeFinalBoundary() {
        StringBuilder buf = new StringBuilder();
        buf.append("--").append(boundary).append("--").append(NEW_LINE);

        logger.getBuffer().writeIndented(buf.toString());

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
        buf.append("Content-Disposition: form-data; name=\"").append(name).append("\"").append(NEW_LINE);
        // buf.append("Content-Type: text/plain; charset=utf-8").append(NEW_LINE);
        buf.append(NEW_LINE);

        logger.getBuffer().writeIndentedLines(buf.toString());
        if (HttpEasyDefaults.getSensitiveParameters().contains(name)) {
            logger.getBuffer().writeLine("*****");
        } else {
            logger.getBuffer().writeLine(String.valueOf(value));
        }

        buf.append(String.valueOf(value)).append(NEW_LINE);

        writer.append(buf);
        writer.flush();
    }

    /**
     * Adds a upload file section to the request.
     *
     * @param fieldName  name attribute in <input type="file" name="..." />
     * @param uploadFile a File to be uploaded
     * @param type       MediaType of the file
     * @throws IOException If unable to read the response
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
     * @throws IOException If unable to read the response
     */
    private void addFilePart(String fieldName, InputStream inputStream, MediaType type, String fileName) throws IOException {
        StringBuilder buf = new StringBuilder();

        writeFieldBoundary();
        buf.append("Content-Disposition: form-data; name=\"").append(fieldName).append("\"; filename=\"").append(fileName).append("\"").append(NEW_LINE);
        if (type == null) {
            buf.append("Content-Type: ").append(URLConnection.guessContentTypeFromName(fileName)).append(NEW_LINE);
        } else {
            buf.append("Content-Type: ").append(type.toString()).append(NEW_LINE);
        }

        // buf.append("Content-Transfer-Encoding: binary").append(NEW_LINE);
        buf.append(NEW_LINE);

        logger.getBuffer().writeIndentedLines(buf.toString());
        logger.getBuffer().writeLine("... Content of file ").write(fileName).writeLine(" ...");

        writer.append(buf);
        writer.flush();

        byte[] buffer = new byte[4096];
        int bytesRead = -1;
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, bytesRead);
        }
        outputStream.flush();

        writer.append(NEW_LINE);
        writer.flush();
    }
}
