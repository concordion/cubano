package org.concordion.cubano.driver.http;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;

import org.concordion.cubano.utils.Config;

/**
 * Reads and supplies properties from the <code>config.properties</code> file that are required by the framework.
 * <p>
 * An optional <code>user.properties</code> file can set user specific values and allow overriding of defaults.
 * The <code>user.properties</code> file should NEVER be checked into source control.
 * <p>
 * This class can be extended by an <code>AppConfig</code> class to provide application specific properties.
 *
 * TODO Should this be singleton?
 *
 * @author Andrew Sumner
 */
public class HttpEasyConfig extends Config {

	protected HttpEasyConfig() {
    		super();
    }
    
    protected HttpEasyConfig(Properties properties) {
        super(properties);
    }

    protected HttpEasyConfig(Properties properties, Properties userProperties) {
        super(properties, userProperties);
    }
       
    private static class WDCHolder {
        static final HttpEasyConfig INSTANCE = new HttpEasyConfig();
    }

    public static HttpEasyConfig getInstance() {
        return WDCHolder.INSTANCE;
    }

    protected void loadProperties() {
   
    }
}