package org.concordion.cubano.driver.web;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.concordion.slf4j.ext.ReportLogger;
import org.concordion.slf4j.ext.ReportLoggerFactory;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.events.WebDriverEventListener;

/**
 * Listens for WebDriver events and logs them.
 * <p>
 * <p>
 * Requires Selenium 2.23.0 or later to retrieve element names from WebElement.toString().
 * </p>
 *
 * @author Andrew Sumner
 */
public class SeleniumEventLogger implements WebDriverEventListener {
    private static final ReportLogger LOGGER = ReportLoggerFactory.getReportLogger(SeleniumEventLogger.class);
    private static final String FUNKY_ARROW = "&#8658;";

    private String originalValue;
    private By prevBy = null;
    private WebElement prevElement = null;
    private String prevScript = null;
    private String prevError = null;

    // private String getElementName(WebElement element) {
    // if (element == null) {
    // return "";
    // }
    //
    // String[] names = element.toString().split("[\\[\\]]");
    // String name = names[names.length - 1];
    // if (name.startsWith(" -> ")) {
    // name = name.substring(4);
    // }
    //
    // int pos = name.indexOf(":");
    // if (pos > 0) {
    // String remainder = name.substring(pos);
    // names = name.substring(0, pos).toString().split(" ");
    // name = names[0];
    //
    // for (int i = 1; i < names.length; i++) {
    // name += names[i].substring(0, 1).toUpperCase() + names[i].substring(1);
    // }
    //
    // name += remainder;
    // }
    //
    // return name;
    // }

    private String getElementName(WebElement element) {
        if (element == null) {
            return "";
        }

        String foundBy = element.toString();
        if (foundBy != null) {
            int arrowIndex = foundBy.indexOf("->");
            if (arrowIndex >= 0) {
                return "By." + foundBy.substring(arrowIndex + 3, foundBy.length() - 1).replace("->", FUNKY_ARROW);
            }
        }

        return "unknown";
    }

    private String getBy(By by) {
        if (by == null) {
            return "";
        }

        return by.toString();
    }

    // Get the earliest location in the stack trace of a selenium or webdriver proxy entry as the item before that "should" be
    // the location of the command that caused this event to happen
    private String getParentLocation() {
        StackTraceElement[] stack = Thread.currentThread().getStackTrace();
        int foundIndex = -1;
        List<String> proxyNames = new ArrayList<>();

        for (int i = 0; i < stack.length; i++) {
            String className = stack[i].getClassName();

            // TODO Not sure if still need this, if do need full class name
            // if (className.contains("FluentWait") || className.contains("WebDriverWait")) {
            // loopCount++;
            //
            // if (loopCount > 1) {
            // continue;
            // }
            // }
            // || className.startsWith("org.concordion.cubano.driver.web.pagefactory.PageObjectAwareHtmlElementsLoader")

            if (className.startsWith("com.sun.proxy.$Proxy")) {
                proxyNames.add(className);

                foundIndex = i;
            } else if (className.startsWith("org.openqa.selenium.support.")) {
                foundIndex = i;
            }
        }

        if (foundIndex > 0) {
            String className = stack[foundIndex].getClassName();

            // Location aware logging always from first className encountered in the stack trace, this entry is further down
            // the stack trace so we would not get a useful location
            if (className.startsWith("com.sun.proxy.$Proxy")) {
                if (proxyNames.stream().filter(s -> s.equals(className)).count() > 1) {
                    return null;
                }
            }

            return className;
        }

        return null;
    }

    @Override
    public void beforeNavigateTo(String url, WebDriver driver) {
        LOGGER.with()
                .message("Navigating to {}", url)
                .htmlMessage("Navigating to {} <a class=\"greyed\" href=\"{}\">{}</a>", FUNKY_ARROW, url, url)
                .locationAwareParent(getParentLocation())
                .debug();
    }

    @Override
    public void afterNavigateTo(String url, WebDriver driver) {
    }

    @Override
    public void beforeNavigateBack(WebDriver driver) {
        LOGGER.with()
                .htmlMessage("Navigating back from {} <span class=\"greyed\">{}</span>", FUNKY_ARROW, driver.getCurrentUrl())
                .locationAwareParent(getParentLocation())
                .trace();
    }

    @Override
    public void afterNavigateBack(WebDriver driver) {
        LOGGER.with()
                .htmlMessage("Navigating back to {} <span class=\"greyed\">{}</span>", FUNKY_ARROW, driver.getCurrentUrl())
                .locationAwareParent(getParentLocation())
                .trace();
    }

    @Override
    public void beforeNavigateForward(WebDriver driver) {
        LOGGER.with()
                .htmlMessage("Navigating forward from {} <span class=\"greyed\">{}</span>", FUNKY_ARROW, driver.getCurrentUrl())
                .locationAwareParent(getParentLocation())
                .trace();
    }

    @Override
    public void afterNavigateForward(WebDriver driver) {
        LOGGER.with()
                .htmlMessage("Navigating forward to {} <span class=\"greyed\">{}</span>", FUNKY_ARROW, driver.getCurrentUrl())
                .locationAwareParent(getParentLocation())
                .trace();
    }

    @Override
    public void beforeFindBy(By by, WebElement element, WebDriver driver) {
        // Repeated FindBys can be generated for WebDriverWait - just display first one
        if (prevBy != null) {
            if (prevBy.equals(by) && (prevElement == null || prevElement.equals(element))) {
                return;
            }
        }

        prevBy = by;
        prevElement = element;
        prevError = null;

        LOGGER.with()
                .htmlMessage("Find element {} <span class=\"greyed\">{}{}{}</span>", FUNKY_ARROW, getBy(by), (element == null ? "" : " in "), getElementName(element))
                .locationAwareParent(getParentLocation())
                .trace();

        // This event is a good indication that next javascript call is unique
        prevScript = null;
    }

    @Override
    public void afterFindBy(By by, WebElement element, WebDriver driver) {
    }

    @Override
    public void beforeClickOn(WebElement element, WebDriver driver) {
        LOGGER.with()
                .htmlMessage("Click {} <span class=\"greyed\">{}</span>", FUNKY_ARROW, getElementName(element))
                .locationAwareParent(getParentLocation())
                .debug();
    }

    @Override
    public void afterClickOn(WebElement element, WebDriver driver) {
    }


    @Override
    public void beforeChangeValueOf(WebElement element, WebDriver driver, CharSequence[] keysToSend) {
        //originalValue = element.getText();
        originalValue = Arrays.toString(keysToSend);

        // What if the element is not visible anymore?
        if (originalValue.isEmpty()) {
            originalValue = element.getAttribute("value");
        }
    }

    @Override
    public void afterChangeValueOf(WebElement element, WebDriver driver, CharSequence[] keysToSend) {
        String changedValue = "";

        changedValue = Arrays.toString(keysToSend);
//        try {
//            changedValue = element.getText();
//
//            if (changedValue.isEmpty()) {
//                changedValue = element.getAttribute("value");
//            }
//        } catch (StaleElementReferenceException e) {
//            changedValue = "[Could not log change of element, because of a stale element reference exception]";
//            return;
//        }

        String name = getElementName(element);

        // LOGGER.trace("{} - Changed value from '{}' to '{}'", name, originalValue, changedValue);

        LOGGER.with()
                .htmlMessage("Change value {} <span class=\"greyed\">of {} from '{}' to '{}'</span>", FUNKY_ARROW, name, originalValue, changedValue)
                .locationAwareParent(getParentLocation())
                .debug();
    }

    @Override
    public void beforeScript(String script, WebDriver driver) {
        int pos = 80;
        String value;

        // Get the first line or 80 characters
        if (script.length() < pos) {
            value = script;
        } else {
            Pattern p = Pattern.compile("[;\\r\\n]");
            Matcher m = p.matcher(script);

            if (m.find()) {
                if (m.start() < pos) {
                    pos = m.start();
                }
            }

            value = script.substring(0, pos) + "...";
        }

        if (!value.equals(prevScript)) {
            prevScript = value;

            // TODO Can we log with result? What if exception occurs?
            // <span class=\"greyed\">true</span>

            LOGGER.with()
                    .htmlMessage("Run JavaScript {}", FUNKY_ARROW)
                    // log all or just value?
                    .data(script)
                    .locationAwareParent(getParentLocation())
                    .trace();
        }

    }

    @Override
    public void afterScript(String script, WebDriver driver) {
    }

    @Override
    public void onException(Throwable throwable, WebDriver driver) {
        if (prevError != null) {
            if (prevError.equals(throwable.getClass().getName())) {
                return;
            }
        }

        prevError = throwable.getClass().getName();

        LOGGER.with()
                .htmlMessage("Selenium Error {} <span class=\"greyed\">{}</span>",
                        FUNKY_ARROW,
                        throwable.getMessage()
                                .replace("<", "&lt;")
                                .replace(">", "&gt;")
                                .replace("\r\n", "<br />")
                                .replace("\n", "<br />"))
                .locationAwareParent(getParentLocation())
                .trace();
    }

    @Override
    public void beforeNavigateRefresh(WebDriver driver) {
        LOGGER.trace("Refreshing Page");
    }

    @Override
    public void afterNavigateRefresh(WebDriver driver) {
    }

    @Override
    public void beforeAlertAccept(WebDriver driver) {
        // TODO Auto-generated method stub

    }

    @Override
    public void afterAlertAccept(WebDriver driver) {
        // TODO Auto-generated method stub

    }

    @Override
    public void afterAlertDismiss(WebDriver driver) {
        // TODO Auto-generated method stub

    }

    @Override
    public void beforeAlertDismiss(WebDriver driver) {
        // TODO Auto-generated method stub

    }
}
