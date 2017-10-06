package org.concordion.cubano.utils;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import java.util.Properties;

import org.junit.Test;

public class ConfigTest {


    @Test
    public void proxySettingsObtainedInOrder() {
    		Properties properties = mock(Properties.class);
    		
//    		The hostname, or address, of the proxy server.
//
//    		Will be populated from the following locations in this order:
    		
//    		1. user.properties file proxy.host setting
//    		1. config.properties file proxy.host setting 
//    		1. System property http.proxyHost
//    		1. Environment variable HTTP_PROXY 
    		given(System.getenv("HTTP_PROXY")).willReturn("http://me1:secret1@proxyhost1:999");
    	    
    	
    		Config config = new ConfigMock(properties);
    		
		assertThat(config.getProxyHost(), is("proxyhost1"));
		assertThat(config.getProxyPort(), is("9991"));
		assertThat(config.getProxyAddress(), is("proxyhost1:9991"));
		assertThat(config.getProxyUser(), is("me"));
		assertThat(config.getProxyPassword(), is("secret"));
		    		
    		
//        given(properties.getProperty("proxy.host")).willReturn("myproxyhost");
//        given(properties.getProperty("proxy.port")).willReturn("9999");
//        given(properties.getProperty("proxy.username")).willReturn("mydomain\\me");
//        given(properties.getProperty("proxy.password")).willReturn("secret");

//        WebDriverConfig config = new WebDriverConfig(properties);
//
//        assertThat(config.isProxyRequired(), is(true));
//        assertThat(config.getProxyHost(), is("myproxyhost"));
//        assertThat(config.getProxyPort(), is("9999"));
//        assertThat(config.getProxyAddress(), is("myproxyhost:9999"));
//        assertThat(config.getProxyUser(), is("mydomain\\me"));
//        assertThat(config.getProxyPassword(), is("secret"));
    	
    	
    }
}
