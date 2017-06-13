package org.concordion.cubano.data;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.apache.commons.io.IOUtils;

/**
 * Utility class for reading files.
 *
 * @author Andrew Sumner
 */
public class FileReader {

    private FileReader() {
    }

    /**
     * Read the file into a string.
     *
     * @param filename Package/FileName of file
     * @return File content or null if file not found
     * @throws IOException
     */
    public static String readFile(String filename) throws IOException {
        String result = null;

        try (InputStream input = FileReader.class.getClassLoader().getResourceAsStream(filename)) {
            if (input == null) {
                throw new IllegalArgumentException("File not found: " + filename);
            }

            result = IOUtils.toString(input, StandardCharsets.UTF_8.name());
        }

        return result;
    }

    /**
     * Read the file into a list of strings.
     *
     * @param filename Package/FileName of file
     * @return File content or null if file not found
     * @throws IOException
     */
    @SuppressWarnings("unchecked")
    public static List<String> readLines(String filename) throws IOException {
        List<String> result = null;

        try (InputStream input = FileReader.class.getClassLoader().getResourceAsStream(filename)) {
            if (input == null) {
                throw new IllegalArgumentException("File not found: " + filename);
            }

            result = IOUtils.readLines(input, StandardCharsets.UTF_8.name());
        }

        return result;
    }
}
