package org.concordion.cubano.driver.http;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/*
 * If this proves to be insufficent look at https://github.com/MarkusBernhardt/proxy-vole - supports PAC
 * http://www.rgagnon.com/javadetails/java-0085.html   
 * https://stackoverflow.com/questions/4933677/detecting-windows-ie-proxy-setting-using-java?utm_medium=organic&utm_source=google_rich_qa&utm_campaign=google_rich_qa
 */

/**
 * Automatically detect proxy.
 * 
 * This does not support proxy automation script (PAC).
 * 
 * @see <a href="https://docs.oracle.com/javase/8/docs/technotes/guides/net/proxies.html">Java Networking and Proxies</a>
 *
 */
public class ProxySearch {
    static {
        System.setProperty("java.net.useSystemProxies", "true");
    }

    private final ProxySelector selector;
    private int maxSize = 20;

    @SuppressWarnings("serial")
    private final Map<String, Proxy> cache = new LinkedHashMap<String, Proxy>() {
        @Override
        protected boolean removeEldestEntry(Map.Entry<String, Proxy> eldest) {
            return size() > maxSize;
        }
    };

    public ProxySearch() {
        this(ProxySelector.getDefault());
    }

    public ProxySearch(ProxySelector selector) {
        this.selector = selector;
    }

    public Proxy select(URL url) {
        String path = String.format("%s://%s", url.getProtocol(), url.getHost());
        if (cache.containsKey(path)) {
            return cache.get(path);
        }

        List<Proxy> l = selector.select(getUri(url));

        if (l != null) {
            Iterator<Proxy> iter = l.iterator();

            // TODO what happens with multiple proxies? 
            while (iter.hasNext()) {
                Proxy proxy = iter.next();

                System.out.println("proxy hostname : " + proxy.type());

                InetSocketAddress addr = (InetSocketAddress) proxy.address();
                if (addr == null) {
                    System.out.println("No Proxy");
                } else {
                    System.out.println("proxy hostname : " + addr.getHostName());
                    System.out.println("proxy port : " + addr.getPort());
                }

                cache.put(path, proxy);
                return proxy;
            }
        }

        return Proxy.NO_PROXY;
    }

    private URI getUri(URL url) {
        try {
            return url.toURI();
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException(e);
        }
    }
}
