package org.concordion.cubano.config;

import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

/**
 * A {@link Properties} implementation that ignores the case of the properties.
 * 
 * The keys are stored untouched so that if you need the original case you can still get them 
 * using <code>Enumeration&lt;String&gt; en = (Enumeration&lt;String&gt;) properties.propertyNames();</code>
 *
 * @author Andrew Sumner
 */
public class CaselessProperties extends Properties {
    private static final long serialVersionUID = 4112578634029874840L;

    Map<String, String> lookup = new TreeMap<String, String>(String.CASE_INSENSITIVE_ORDER);

    @Override
    public synchronized Object put(Object key, Object value) {
        lookup.put(((String) key), (String) key);

        return super.put(key, value);
    }

    @Override
    public String getProperty(String key) {
        String realKey = lookup.get(key);

        return super.getProperty(realKey == null ? key : realKey);
    }

    @Override
    public String getProperty(String key, String defaultValue) {
        String realKey = lookup.get(key);

        return super.getProperty(realKey == null ? key : realKey, defaultValue);
    }
}
