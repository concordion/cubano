package org.concordion.cubano.utils;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import java.io.IOException;
import java.io.StringReader;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Map;
import java.util.Properties;

import javax.annotation.Generated;

import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.RestoreSystemProperties;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest({Config.class})
public class ConfigTest {
	
	@Rule
    public final RestoreSystemProperties restoreSystemProperties = new RestoreSystemProperties();
	
	@Rule
	public final ExpectedException exception = ExpectedException.none();

	private Enumeration<String> empty = Collections.enumeration(Collections.<String>emptyList());

	@Test
    public void proxySettingsObtainedInOrder() {
    		Properties properties = givenDefaultProperties();
    			
		Config config = new ConfigMock(properties);
		
		assertThat(config.getProxyHost(), is("proxyhost1"));
		assertThat(config.getProxyPort(), is(9991));
		assertThat(config.getProxyAddress(), is("proxyhost1:9991"));
		assertThat(config.getProxyUser(), is("me1"));
		assertThat(config.getProxyPassword(), is("secret1"));
		// TODO can't get mock to override System.getProperty above
		// assertThat(config.getNonProxyHosts(), is("no1"));
		
		// 2. System property http.proxyHost
		System.setProperty("http.proxyHost", "proxyhost2");		
//		System.setProperty("http.proxyPort", "9992");
		System.setProperty("http.proxyUser", "domain\\me2");
//		System.setProperty("http.proxyPassword", "secret2");
		System.setProperty("http.nonProxyHosts", "no2");
		
       
		config = new ConfigMock(properties);		

		assertThat(config.getProxyHost(), is("proxyhost2"));
		assertThat(config.getProxyPort(), is(9991));
		assertThat(config.getProxyAddress(), is("proxyhost2:9991"));
		assertThat(config.getProxyUser(), is("domain\\me2"));
		// TODO Nigel: Note: this came from HTTP_PROXY rather than http.proxyPassword
		assertThat(config.getProxyPassword(), is("secret1"));
		assertThat(config.getNonProxyHosts(), is("no2"));
		
		// 1. config.properties and/or user.propertes file proxy.host setting 
		given(properties.getProperty("proxy.host")).willReturn("proxyhost3");
		given(properties.getProperty("proxy.port")).willReturn(null);
		given(properties.getProperty("proxy.username")).willReturn("me3");
		given(properties.getProperty("proxy.password")).willReturn(null);
		given(properties.getProperty("proxy.nonProxyHosts")).willReturn("no3");
		
		config = new ConfigMock(properties);		

		assertThat(config.getProxyHost(), is("proxyhost3"));
		assertThat(config.getProxyPort(), is(9991));
		assertThat(config.getProxyAddress(), is("proxyhost3:9991"));
		assertThat(config.getProxyUser(), is("me3"));
		assertThat(config.getProxyPassword(), is("secret1"));
		assertThat(config.getNonProxyHosts(), is("no3"));
    }
	
	@Test
    public void proxyFromConfigFile() {
    		Properties properties = givenDefaultProperties();
    
    		given(properties.getProperty("proxy.host")).willReturn("proxyhost4");
		given(properties.getProperty("proxy.port")).willReturn(null);
		given(properties.getProperty("proxy.username")).willReturn("me4");
		given(properties.getProperty("proxy.password")).willReturn("secret4");
		given(properties.getProperty("proxy.nonProxyHosts")).willReturn("no4");
		
		Config config = new ConfigMock(properties);		

		assertThat(config.getProxyHost(), is("proxyhost4"));
		assertThat(config.getProxyPort(), is(80));
		assertThat(config.getProxyAddress(), is("proxyhost4"));
		assertThat(config.getProxyUser(), is("me4"));
		assertThat(config.getProxyPassword(), is("secret4"));
		assertThat(config.getNonProxyHosts(), is("no4"));
    }
	
	@Test
    public void proxyFromSystemProperties() {
    		Properties properties = givenDefaultProperties();
    			
		// 2. System property http.proxyHost
		System.setProperty("http.proxyHost", "proxyhost3");		
		// System.setProperty("http.proxyPort", null);
		System.setProperty("http.proxyUser", "domain\\me3");
		System.setProperty("http.proxyPassword", "secret3");
		System.setProperty("http.nonProxyHosts", "no3");
		
		Config config = new ConfigMock(properties);		

		assertThat(config.getProxyHost(), is("proxyhost3"));
		// This is the default value
		assertThat(config.getProxyPort(), is(80));
		assertThat(config.getProxyAddress(), is("proxyhost3"));
		assertThat(config.getProxyUser(), is("domain\\me3"));
		assertThat(config.getProxyPassword(), is("secret3"));
		assertThat(config.getNonProxyHosts(), is("no3"));
    }

    @Test
    public void proxyFromHTTP_PROXY() {
    		Properties properties = givenDefaultProperties();
    		
    		PowerMockito.mockStatic(System.class);
    		PowerMockito.when(System.getenv(Mockito.eq("HTTP_PROXY"))).thenReturn("http://me1:secret1@proxyhost1:9991");
    		//PowerMockito.when(System.getenv(Mockito.eq("NO_PROXY"))).thenReturn("no1");
    		
        PowerMockito.when(System.getProperty(Mockito.any())).thenCallRealMethod();
        PowerMockito.when(System.getProperty(Mockito.any(), Mockito.any())).thenCallRealMethod();
        PowerMockito.when(System.setProperty(Mockito.any(), Mockito.any())).thenCallRealMethod();
        		
		Config config = new ConfigMock(properties);
		
		assertThat(config.getProxyHost(), is("proxyhost1"));
		assertThat(config.getProxyPort(), is(9991));
		assertThat(config.getProxyAddress(), is("proxyhost1:9991"));
		assertThat(config.getProxyUser(), is("me1"));
		assertThat(config.getProxyPassword(), is("secret1"));
		
		// Using default setting
		assertThat(config.getNonProxyHosts(), is(Config.DEFAULT_NON_PROXY_HOSTS));
    }
    
    @Test
    public void proxyFromHTTP_PROXY_USER() {
		Properties properties = givenDefaultProperties();
		
		PowerMockito.mockStatic(System.class);
		PowerMockito.when(System.getenv(Mockito.eq("HTTP_PROXY"))).thenReturn("http://proxyhost2:9992");
		PowerMockito.when(System.getenv(Mockito.eq("HTTP_PROXY_USER"))).thenReturn("me2");
		PowerMockito.when(System.getenv(Mockito.eq("HTTP_PROXY_PASS"))).thenReturn("secret2");
		PowerMockito.when(System.getenv(Mockito.eq("NO_PROXY"))).thenReturn("no2");

		PowerMockito.when(System.getProperty(Mockito.any())).thenCallRealMethod();
        PowerMockito.when(System.getProperty(Mockito.any(), Mockito.any())).thenCallRealMethod();
        PowerMockito.when(System.setProperty(Mockito.any(), Mockito.any())).thenCallRealMethod();
        
		Config config = new ConfigMock(properties);

		assertThat(config.getProxyHost(), is("proxyhost2"));
		assertThat(config.getProxyPort(), is(9992));
		assertThat(config.getProxyAddress(), is("proxyhost2:9992"));
		assertThat(config.getProxyUser(), is("me2"));
		assertThat(config.getProxyPassword(), is("secret2"));
		assertThat(config.getNonProxyHosts(), is("no2"));
    }
    
    @Test
    public void proxyMustBeConfiguredIfRequired() {
    		Properties properties = givenDefaultProperties();

    		given(properties.getProperty("proxy.required")).willReturn("true");
    		
    		exception.expect(IllegalArgumentException.class);
        @SuppressWarnings("unused")
		Config config = new ConfigMock(properties);
    }

    
    @Test
    public void canSetProxyPropertiesEvenIfProxyIsFalse() {
    		Properties properties = givenDefaultProperties();

		given(properties.getProperty("proxy.required")).willReturn("false");
		given(properties.getProperty("proxy.host")).willReturn("myproxyhost1");
        
		Config config = new ConfigMock(properties);
    
        assertThat(config.isProxyRequired(), is(false));
        assertThat(config.getProxyHost(), is("myproxyhost1"));
    }
    
    @Test
    public void mustSetEnvrionment() throws Exception {
        Properties properties = mock(Properties.class);
        
        exception.expect(IllegalArgumentException.class);
        @SuppressWarnings("unused")
        Config config = new ConfigMock(properties);
    }
    
    @Test
    public void systemPropertyWillOverrideEnvrionment() throws Exception {
        Properties properties = givenDefaultProperties();

        System.setProperty("environment", "SIT");
	
        Config config = new ConfigMock(properties);

        assertThat(config.getEnvironment(), is("SIT"));
    }
    
    @Test
    public void mustSetDefaultProperties() throws Exception {
        Properties properties = givenDefaultProperties();

        Config config = new ConfigMock(properties);

        assertThat(config.getEnvironment(), is("UAT"));
        assertThat(config.isProxyRequired(), is(false));
        assertThat(config.getProxyHost(), is(""));
        assertThat(config.getProxyPort(), is(80));
        assertThat(config.getProxyAddress(), is(""));
        assertThat(config.getProxyUser(), is(""));
        assertThat(config.getProxyPassword(), is(""));
        
        // TODO Fails on my laptop as getting value "local,*.local,169.254/16,*.169.254/16,127.0.0.1" from somewhere
        // assertThat(config.getNonProxyHosts(), is(""));
    }
    
    @SuppressWarnings("unchecked")
	@Test
    public void userPropertiesOverrideConfigProperties() {
        Properties properties = givenDefaultProperties();        
        given(properties.getProperty("a.setting")).willReturn("false");

        Properties userProperties = mock(Properties.class);
        given(userProperties.getProperty("a.setting")).willReturn("true");
        given((Enumeration<String>)userProperties.propertyNames()).willReturn(empty);

        Config config = new ConfigMock(properties);
        assertThat(config.getProperty("a.setting"), is("false"));
        
        config = new ConfigMock(properties, userProperties);
        assertThat(config.getProperty("a.setting"), is("true"));
    }

    @Test
    public void environmentPropertiesOverrideDefaultProperties() {
    		Properties properties = givenDefaultProperties();
    		given(properties.getProperty("a.setting")).willReturn("false");
        given(properties.getProperty("SIT.a.setting")).willReturn("true");
       
        Config config = new ConfigMock(properties);
        assertThat(config.getProperty("a.setting"), is("false"));
        
        given(properties.getProperty("environment")).willReturn("SIT");        
        config = new ConfigMock(properties);
        assertThat(config.getProperty("a.setting"), is("true"));
    }
    
    @Test
    public void canSearchForPopertiesWithPrefixMatchingCase() throws IOException {
    		Properties properties = givenDefaultSearchProperties();
    		    		
    		Config config = new ConfigMock(properties);
    		
    		Map<String, String> found = config.getPropertiesStartingWith("a.setting.");
    		
    		assertThat(found.values().size(), is(1));
    		assertThat(found.keySet().iterator().next(), is("a.setting.2"));
    }
    
    @Test
    public void canSearchForPopertiesAngGetOriginalPropertyCase() throws IOException {
    		Properties properties = givenDefaultSearchProperties();
    		    		
    		Config config = new ConfigMock(properties);
    		
    		Map<String, String> found = config.getPropertiesStartingWith("a.");
    		
    		assertThat(found.values().size(), is(2));
    		assertThat(found.keySet().iterator().next(), is("a.SETTING.1"));
    }

    @Test
    public void canSearchForPopertiesAngGetKeysWithoutPrefix() throws IOException {
    		Properties properties = givenDefaultSearchProperties();
    		    		
    		Config config = new ConfigMock(properties);
    		
    		Map<String, String> found = config.getPropertiesStartingWith("a.setting.", true);
    		
    		assertThat(found.values().size(), is(1));
    		assertThat(found.keySet().iterator().next(), is("2"));
    }
    
	@SuppressWarnings("unchecked")
	private Properties givenDefaultProperties() {
	    Properties properties = mock(Properties.class);
	    
	    given(properties.getProperty("environment")).willReturn("UAT");
	    given((Enumeration<String>)properties.propertyNames()).willReturn(empty);
	    
	    return properties;
	}
	
	private Properties givenDefaultSearchProperties() throws IOException {
		CaselessProperties properties = new CaselessProperties();
		StringBuilder sb = new StringBuilder();
		
		String newline = System.lineSeparator();
		sb.append("environment=UAT").append(newline);
		sb.append("a.SETTING.1=value1").append(newline);
		sb.append("a.setting.2=value2").append(newline);
		sb.append("b.SETTING.1=value1").append(newline);
	
		properties.load(new StringReader(sb.toString()));
	    
	    return properties;
	}
}
