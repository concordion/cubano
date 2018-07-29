package org.concordion.cubano.framework;

import org.concordion.cubano.driver.web.Browser;
import org.concordion.cubano.driver.web.provider.BrowserProvider;
import org.junit.Test;
import org.openqa.selenium.WebDriver;

import java.io.IOException;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

public class ConcordionBrowserFixtureTest {
    private ConcordionBrowserFixture test1 = new ConcordionBrowserFixture() {};

    @Test
    public void reusesBrowsersForSameKey() {
        BrowserProvider mockProvider = mock(BrowserProvider.class);

        Browser browser1 = test1.getBrowser("X", mockProvider);
        Browser browser2 = test1.getBrowser("X", mockProvider);

        assertThat(browser1, is(browser2));
    }

    @Test
    public void closesMultipleDriversForSingleBrowserProvider() throws IOException {
        BrowserProvider mockProvider = mock(BrowserProvider.class);
        WebDriver mockDriver = mock(WebDriver.class);
        when(mockProvider.createDriver()).thenReturn(mockDriver);

        Browser browser1 = test1.getBrowser("A", mockProvider);
        browser1.getDriver();
        Browser browser2 = test1.getBrowser("B", mockProvider);
        browser2.getDriver();
        test1.closeSuiteResources();

        verify(mockProvider, times(1)).close();
        verify(mockDriver, times(2)).quit();
    }

    @Test
    public void closesMultipleDriversAndBrowserProviders() throws IOException {
        BrowserProvider mockProvider1 = mock(BrowserProvider.class);
        WebDriver mockDriver1 = mock(WebDriver.class);
        when(mockProvider1.createDriver()).thenReturn(mockDriver1);
        BrowserProvider mockProvider2 = mock(BrowserProvider.class);
        WebDriver mockDriver2 = mock(WebDriver.class);
        when(mockProvider2.createDriver()).thenReturn(mockDriver2);

        Browser browser1 = test1.getBrowser("E", mockProvider1);
        browser1.getDriver();
        Browser browser2 = test1.getBrowser("F", mockProvider1);
        browser2.getDriver();
        Browser browser3 = test1.getBrowser("G", mockProvider2);
        browser3.getDriver();
        test1.closeSuiteResources();

        verify(mockProvider1, times(1)).close();
        verify(mockDriver1, times(2)).quit();
        verify(mockProvider2, times(1)).close();
        verify(mockDriver2, times(1)).quit();
    }
}
