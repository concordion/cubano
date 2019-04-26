package org.concordion.cubano.driver.web;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.concordion.cubano.driver.BrowserBasedTest;
import org.junit.Test;
import org.openqa.selenium.WebDriver;

public class PageHelperTest {

    @Test
    public void canCreateInstanceOfBasePageObjectUsingExpectedClassOnly() {

        PageHelper tpo = setUpMocks();

        TestPageObject testPageObject = tpo.newInstance(TestPageObject.class);
        assertNotNull("Should not be null when constructing instance of "
                + BasePageObject.class.getName(),
                testPageObject);

    }

    @Test
    public void canCreateInstanceOfBasePageObjectUsingExpectedClassPlusOptionalParams() {

        PageHelper tpo = setUpMocks();

        TestPageObject testPageObject = tpo.newInstance(TestPageObject.class, "HelloWorld", 1);

        assertNotNull("Should not be null when constructing instance of "
                + BasePageObject.class.getName() + " and optional parameters",
                testPageObject);

    }

    @Test
    public void canCreateClassWithBrowserBasedTestConstructorOnly() {

        PageHelper tpo = setUpMocks();

        TestPageObjectNoParams testPageObjectNoParams = tpo.newInstance(TestPageObjectNoParams.class, "BrowserBasedTestConstructorOnly");

        assertNotNull("Should not be null when constructing instance of "
                + BasePageObject.class.getName() + " and class has Browser Based Test Constructor Only",
                testPageObjectNoParams);

    }

    private PageHelper setUpMocks() {
        WebDriver webDriverMock = mock(WebDriver.class);
        Browser browserMock = mock(Browser.class);
        BrowserBasedTest browserBasedTestMock = mock(BrowserBasedTest.class);

        when(browserBasedTestMock.getBrowser()).thenReturn(browserMock);
        when(browserMock.getDriver()).thenReturn(webDriverMock);

        BasePageObject basePageObjectMock = mock(BasePageObject.class);
        when(basePageObjectMock.getTest()).thenReturn(browserBasedTestMock);

        return new PageHelper(basePageObjectMock);
    }
}
