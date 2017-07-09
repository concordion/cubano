package org.concordion.cubano.driver.http;

import com.google.common.net.MediaType;

/**
 * Form field for http request.
 *
 * @author Andrew Sumner
 */
class Field {
    final MediaType type;
    final String name;
    final Object value;
    final String fileName;

    /**
     * Create a new form field.
     *
     * @param name     Field name
     * @param value    Field contents
     * @param type     Field media type
     * @param fileName Field file name for attachments
     */
    Field(String name, Object value, MediaType type, String fileName) {
        this.name = name;
        this.value = value;
        this.type = type;
        this.fileName = fileName;
    }
}
