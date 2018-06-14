package org.concordion.cubano.driver.http;

import java.io.IOException;

/*
 * If this proves to be insufficent look at https://github.com/MarkusBernhardt/proxy-vole
 * https://stackoverflow.com/questions/4933677/detecting-windows-ie-proxy-setting-using-java?utm_medium=organic&utm_source=google_rich_qa&utm_campaign=google_rich_qa
 */

/**
 * Automatically detect proxy.
 * 
 * @see <a href="https://docs.oracle.com/javase/8/docs/technotes/guides/net/proxies.html">Java Networking and Proxies</a>
 *
 */
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.SocketAddress;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;

public class ProxySearchTrial extends ProxySelector {

    // Keep a reference on the previous default
    final ProxySelector selector;

    /*
     * Inner class representing a Proxy and a few extra data
     */
    class InnerProxy {
        Proxy proxy;
        SocketAddress addr;
        // How many times did we fail to reach this proxy?
        int failedCount = 0;

        InnerProxy(InetSocketAddress a) {
            addr = a;
            proxy = new Proxy(Proxy.Type.HTTP, a);
        }

        SocketAddress address() {
            return addr;
        }

        Proxy toProxy() {
            return proxy;
        }

        int failed() {
            return ++failedCount;
        }
    }

    /*
     * A list of proxies, indexed by their address.
     */
    HashMap<SocketAddress, InnerProxy> proxies = new HashMap<SocketAddress, InnerProxy>();

    public ProxySearchTrial() {
        this(ProxySelector.getDefault());
    }

    public ProxySearchTrial(ProxySelector selector) {
        this.selector = selector;
    }

    /*
     * This is the method that the handlers will call.
     * Returns a List of proxy.
     */
    public java.util.List<Proxy> select(URI uri) {
        // Let's stick to the specs.
        if (uri == null) {
            throw new IllegalArgumentException("URI can't be null.");
        }

        /*
         * If it's a http (or https) URL, then we use our own
         * list.
         */
        String protocol = uri.getScheme();
        if ("http".equalsIgnoreCase(protocol) || "https".equalsIgnoreCase(protocol)) {
            ArrayList<Proxy> l = new ArrayList<Proxy>();

            for (InnerProxy p : proxies.values()) {
                l.add(p.toProxy());
            }

            return l;
        }

        /*
         * Not HTTP or HTTPS (could be SOCKS or FTP)
         * defer to the default selector.
         */
        if (selector != null) {
            return selector.select(uri);
        } else {
            ArrayList<Proxy> l = new ArrayList<Proxy>();
            l.add(Proxy.NO_PROXY);
            return l;
        }
    }

    /*
     * Method called by the handlers when it failed to connect
     * to one of the proxies returned by select().
     */
    public void connectFailed(URI uri, SocketAddress sa, IOException ioe) {
        // Let's stick to the specs again.
        if (uri == null || sa == null || ioe == null) {
            throw new IllegalArgumentException("Arguments can't be null.");
        }

        /*
         * Let's lookup for the proxy
         */
        InnerProxy p = proxies.get(sa);
        if (p != null) {
            /*
             * It's one of ours, if it failed more than 3 times
             * let's remove it from the list.
             */
            if (p.failed() >= 3)
                proxies.remove(sa);
        } else {
            /*
             * Not one of ours, let's delegate to the default.
             */
            if (selector != null)
                selector.connectFailed(uri, sa, ioe);
        }
    }
}