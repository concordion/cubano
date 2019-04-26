package org.concordion.cubano.driver.web;

import org.concordion.cubano.driver.BrowserBasedTest;

public class TestPageObject extends BasePageObject<TestPageObject>{
    protected TestPageObject(BrowserBasedTest test, int timeoutWaitInSeconds, Object[] params) {
        super(test, timeoutWaitInSeconds, params);

    }

    protected TestPageObject(BrowserBasedTest test) {
        super(test, 10);

    }

    protected TestPageObject(BrowserBasedTest test, Object[] params) {
        super(test, 10, params);

    }

    @Override
    protected void waitUntilPageIsLoaded(int timeoutWaitInSeconds, Object... params) {


    }

}
