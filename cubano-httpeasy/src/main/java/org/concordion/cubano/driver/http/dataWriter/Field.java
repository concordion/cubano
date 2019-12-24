package org.concordion.cubano.driver.http.dataWriter;

import com.google.common.net.MediaType;

/**
 * Form field for http request.
 *
 * @author Andrew Sumner
 */
public class Field {
    public final MediaType type;
    public final String name;
    public final Object value;
    public final String fileName;

    /**
     * Create a new form field.
     *
     * @param name     Field name
     * @param value    Field contents
     * @param type     Field media type
     * @param fileName Field file name for attachments
     */
    public Field(String name, Object value, MediaType type, String fileName) {
        this.name = name;
        this.value = value;
        this.type = type;
        this.fileName = fileName;
    }
}
