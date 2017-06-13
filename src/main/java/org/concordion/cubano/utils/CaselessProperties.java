package org.concordion.cubano.utils;

import java.util.Properties;

/**
 * A {@link Properties} implementation that ignores the case of the properties.
 *
 * @author Andrew Sumner
 */
public class CaselessProperties extends Properties {
    private static final long serialVersionUID = 4112578634029874840L;

    @Override
    public Object put(Object key, Object value) {
        String lowercase = ((String) key).toLowerCase();
        return super.put(lowercase, value);
    }

    @Override
    public String getProperty(String key) {
        String lowercase = key.toLowerCase();
        return super.getProperty(lowercase);
    }

    @Override
    public String getProperty(String key, String defaultValue) {
        String lowercase = key.toLowerCase();
        return super.getProperty(lowercase, defaultValue);
    }
}
