package org.concordion.cubano.driver.web;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.concordion.cubano.driver.BrowserBasedTest;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.openqa.selenium.WebDriver;

public class PageHelperTest {

    @Test
    public void canCreateInstanceOfPageObjectUsingBBTOnly() {

        PageHelper tpo = setUpMocks();

        TestPageObject testPageObject = tpo.newInstance(TestPageObject.class);
        assertNotNull("Should not be null when constructing instance of "
                + TestPageObject.class.getName(),
                testPageObject);

    }

    @Test
    public void canCreateInstanceOfPageObjectUsingBBTPlusMultipleParams() {

        PageHelper tpo = setUpMocks();

        TestPageObject testPageObject = tpo.newInstance(TestPageObject.class, "HelloWorld", 1);

        assertNotNull("Should not be null when constructing instance of "
                + TestPageObject.class.getName() + " and optional parameters",
                testPageObject);

    }

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void shouldFailAsNoParamsConstructorForPageObject() {

        PageHelper tpo = setUpMocks();

        thrown.expect(RuntimeException.class);
        thrown.expectCause(allOf(
                instanceOf(NoSuchMethodException.class),
                hasProperty("message",
                        is(anyOf(is("org.concordion.cubano.driver.web.TestPageObjectNoParams.<init>(org.concordion.cubano.driver.BrowserBasedTest, [Ljava.lang.Object;)"),
                                is("org.concordion.cubano.driver.web.TestPageObjectNoParams.<init>(org.concordion.cubano.driver.BrowserBasedTest,[Ljava.lang.Object;)"))))));

        tpo.newInstance(TestPageObjectNoParams.class, "ShouldFailAsNoParamsConstructor");
    }

    @Test
    public void canCreateInstanceOfPageObjectWhichOnlyHasBBTConstructor() {

        PageHelper tpo = setUpMocks();

        TestPageObjectNoParams testPageObjectNoParams = tpo.newInstance(TestPageObjectNoParams.class);

        assertNotNull("Should not be null when constructing instance of "
                + TestPageObjectNoParams.class.getName() + " and class has Browser Based Test Constructor Only",
                testPageObjectNoParams);

    }

    private PageHelper setUpMocks() {
        WebDriver webDriverMock = mock(WebDriver.class);
        Browser browserMock = mock(Browser.class);
        BrowserBasedTest browserBasedTestMock = mock(BrowserBasedTest.class);

        when(browserBasedTestMock.getBrowser()).thenReturn(browserMock);
        when(browserMock.getDriver()).thenReturn(webDriverMock);

        BasePageObject<?> basePageObjectMock = mock(BasePageObject.class);
        when(basePageObjectMock.getTest()).thenReturn(browserBasedTestMock);

        return new PageHelper(basePageObjectMock);
    }
}
