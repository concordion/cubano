package org.concordion.cubano.utils;

import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

/**
 * A {@link Properties} implementation that ignores the case of the properties.
 * 
 * The keys are stored untouched so that if you need the original case you can still get it using  
 *
 * @author Andrew Sumner
 */
public class CaselessProperties extends Properties {
    private static final long serialVersionUID = 4112578634029874840L;
    
    Map<String, String> lookup = new TreeMap<String, String>(String.CASE_INSENSITIVE_ORDER);

    @Override
    public Object put(Object key, Object value) {
        lookup.put(((String) key).toLowerCase(), (String) key);
       
        return super.put(key, value);
    }

    @Override
    public String getProperty(String key) {
    	return super.getProperty(lookup.get(key));
    }

    @Override
    public String getProperty(String key, String defaultValue) {
    	return super.getProperty(lookup.get(key), defaultValue);
    }
}
