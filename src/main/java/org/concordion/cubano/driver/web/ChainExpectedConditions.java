package org.concordion.cubano.driver.web;

import java.util.ArrayList;
import java.util.List;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;

/**
 * Designed to work with WebDriverWait when you have multiple condition to check on a page.
 * <p>
 * Each condition will be checked in turn until until they all passed or the calling Wait method times out.
 * <p>
 * Once a condition has passed it will not be checked again.
 *
 * @author Andrew Sumner
 */
public class ChainExpectedConditions implements ExpectedCondition<Boolean> {
    private List<Condition> conditions = new ArrayList<Condition>();

    @Override
    public Boolean apply(WebDriver input) {
        for (Condition condition : conditions) {
            if (condition.passed) {
                continue;
            }

            Object result = condition.expected.apply(input);
            if (result instanceof Boolean) {
                if (!(boolean) result) {
                    return false;
                }
            } else if (result == null) {
                return false;
            }

            condition.passed = true;
        }

        return true;
    }

    /**
     * Creates ChainExpectedConditions object and adds first expected condition.  All subsequent
     * conditions should be added by the {@link #and(ExpectedCondition)} method.
     *
     * @param condition First expected condition
     * @return A new ChainExpectedConditions object
     */
    public ChainExpectedConditions and(ExpectedCondition<?> condition) {
        conditions.add(new Condition(condition));
        return this;
    }

    /**
     * Add second (or subsequent) condition.
     *
     * @param condition Expected condition
     * @return A self reference
     */
    public static ChainExpectedConditions with(ExpectedCondition<?> condition) {
        ChainExpectedConditions c = new ChainExpectedConditions();

        return c.and(condition);
    }

    @Override
    public String toString() {
        for (Condition condition : conditions) {
            if (!condition.passed) {
                return condition.expected.toString();
            }
        }

        return super.toString();
    }

    /**
     * Helper class for the state of the conditions.
     */
    private static class Condition {
        private final ExpectedCondition<?> expected;
        private boolean passed = false;

        public Condition(ExpectedCondition<?> condition) {
            this.expected = condition;
        }
    }
}
