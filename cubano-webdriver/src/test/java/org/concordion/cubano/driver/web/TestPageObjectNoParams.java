package org.concordion.cubano.driver.web;

import org.concordion.cubano.driver.BrowserBasedTest;

public class TestPageObjectNoParams extends BasePageObject<TestPageObjectNoParams> {

    protected TestPageObjectNoParams(BrowserBasedTest test) {
        super(test, 10);

    }

    @Override
    protected void waitUntilPageIsLoaded(int timeoutWaitInSeconds, Object... params) {


    }

}
