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

/**
 * Automatically detect proxy.
 * 
 * This does not support proxy automation script (PAC) which is a big show stopper so this class is a waste of time and I'm using proxy-vole instead. However
 * proxy-vole (https://github.com/MarkusBernhardt/proxy-vole) seems like overkill and so this is remaining as a starting point for a lightweight implementation...
 * 
 * @see <a href="https://docs.oracle.com/javase/8/docs/technotes/guides/net/proxies.html">Java Networking and Proxies</a>
 *
 */
public class ProxySearch2 {
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

    public ProxySearch2() {
        this(ProxySelector.getDefault());
    }

    public ProxySearch2(ProxySelector selector) {
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
