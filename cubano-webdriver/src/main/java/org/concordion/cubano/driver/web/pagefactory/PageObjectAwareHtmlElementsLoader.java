package org.concordion.cubano.driver.web.pagefactory;

import static ru.yandex.qatools.htmlelements.utils.HtmlElementUtils.getElementName;
import static ru.yandex.qatools.htmlelements.utils.HtmlElementUtils.newInstance;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.LinkedList;
import java.util.List;

import org.concordion.cubano.driver.web.BasePageObject;
import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.internal.WrapsDriver;
import org.openqa.selenium.support.PageFactory;

import ru.yandex.qatools.htmlelements.element.HtmlElement;
import ru.yandex.qatools.htmlelements.element.TypifiedElement;
import ru.yandex.qatools.htmlelements.exceptions.HtmlElementsException;
import ru.yandex.qatools.htmlelements.loader.HtmlElementLoader;
import ru.yandex.qatools.htmlelements.loader.decorator.HtmlElementLocatorFactory;

/**
 * A helper class for finding custom HtmlElement(s) in the same way that driver.findElement() and driver.findElements() methods work.
 * <p>
 * TODO It needs to pass correct decorator - see TODO on PageObjectAwareHtmlElementDecorator.
 *
 * @author Andrew Sumner
 */
public class PageObjectAwareHtmlElementsLoader implements WrapsDriver {
    private final WebDriver driver;
    private BasePageObject<?> pageObject;

    /**
     * Constructor.
     *
     * @param driver     WebDriver
     * @param pageObject PageObject sitting on
     */
    public PageObjectAwareHtmlElementsLoader(WebDriver driver, BasePageObject<?> pageObject) {
        this.driver = driver;
        this.pageObject = pageObject;
    }

    @Override
    public WebDriver getWrappedDriver() {
        return driver;
    }

    /**
     * Get the named field from the class.
     *
     * @param elementClass Class that field belongs to
     * @param fieldName    Name of field to find
     * @return Field if found, or null if not found.
     */
    public Field getFieldFromClass(Class<?> elementClass, String fieldName) {
        try {
            return elementClass.getDeclaredField(fieldName);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Find all elements within the current page using the given mechanism.
     * This method is affected by the 'implicit wait' times in force at the time of execution. When
     * implicitly waiting, this method will return as soon as there are more than 0 items in the
     * found collection, or will return an empty list if the timeout is reached.
     *
     * @param <T>          A class that extends HtmlElement
     * @param elementClass The specific HtmlElement class wrapping the element
     * @param by           The locating mechanism to use
     * @return A list of all {@link WebElement}s, or an empty list if nothing matches
     * @see org.openqa.selenium.By
     * @see org.openqa.selenium.WebDriver.Timeouts
     */
    public <T extends HtmlElement> List<T> findElements(Class<T> elementClass, By by) {
        return findElements(elementClass, by, null);
    }

    /**
     * Find all elements within the current page using the given mechanism.
     * This method is affected by the 'implicit wait' times in force at the time of execution. When
     * implicitly waiting, this method will return as soon as there are more than 0 items in the
     * found collection, or will return an empty list if the timeout is reached.
     *
     * @param <T>          A class that extends HtmlElement
     * @param elementClass The specific HtmlElement class wrapping the element
     * @param by           The locating mechanism to use
     * @param field        the field that is being updated - used to get the @name annotation
     * @return A list of all {@link WebElement}s, or an empty list if nothing matches
     * @see org.openqa.selenium.By
     * @see org.openqa.selenium.WebDriver.Timeouts
     */
    public <T extends HtmlElement> List<T> findElements(Class<T> elementClass, By by, Field field) {
        String name = null;

        if (field != null) {
            name = getElementName(field);
        }

        List<T> elements = new LinkedList<T>();

        for (WebElement element : driver.findElements(by)) {
            elements.add(createHtmlElement(elementClass, element, name, pageObject));
        }

        return elements;
    }

    //CHECKSTYLE:OFF - Not handling NoSuchElementException

    /**
     * Find the first {@link WebElement} using the given method.
     * This method is affected by the 'implicit wait' times in force at the time of execution.
     * The findElement(..) invocation will return a matching row, or try again repeatedly until
     * the configured timeout is reached.
     * <p>
     * findElement should not be used to look for non-present elements, use {@link WebDriver#findElements(By)}
     * and assert zero length response instead.
     *
     * @param <T>          A class that extends HtmlElement
     * @param elementClass The specific HtmlElement class wrapping the element
     * @param by           The locating mechanism
     * @return The first matching element on the current page
     * @throws org.openqa.selenium.NoSuchElementException If no matching elements are found
     * @see org.openqa.selenium.By
     * @see org.openqa.selenium.WebDriver.Timeouts
     */
    //CHECKSTYLE:ON
    public <T extends HtmlElement> T findElement(Class<T> elementClass, By by) {
        WebElement element = driver.findElement(by);

        return createHtmlElement(elementClass, element, null, pageObject);
    }

    /**
     * Find the first {@link WebElement} using the given method.
     * This method is affected by the 'implicit wait' times in force at the time of execution.
     * The findElement(..) invocation will return a matching row, or try again repeatedly until
     * the configured timeout is reached.
     * <p>
     * findTypifiedElement should not be used to look for non-present elements, use {@link WebDriver#findElements(By)}
     * and assert zero length response instead.
     *
     * @param <T>          A class that extends TypifiedElement
     * @param elementClass The specific TypifiedElement class wrapping the element
     * @param by           The locating mechanism
     * @return The first matching element on the current page
     * @throws org.openqa.selenium.NoSuchElementException If no matching elements are found
     * @see org.openqa.selenium.By
     * @see org.openqa.selenium.WebDriver.Timeouts
     */
    public <T extends TypifiedElement> T findTypifiedElement(Class<T> elementClass, By by) {
        WebElement element = driver.findElement(by);

        T instance = HtmlElementLoader.createTypifiedElement(elementClass, element, "");

        setAwareValue(instance, pageObject);

        return instance;
    }

    /**
     * Creates an instance of the given class representing a single web elements.
     * <p>
     * Is a COPY of the Yandex HtmlElementLoader classes method so can set the page object if required.
     *
     * @param <T>              Typified element type as defined by elementClass
     * @param elementClass     Class of element to create
     * @param elementToWrap    WebElement this class represents
     * @param name             Name of element
     * @param parentPageObject Parent pageObject
     * @return A typified element of the requested class
     */
    public static <T extends TypifiedElement> T createTypifiedElement(Class<T> elementClass, WebElement elementToWrap, String name, BasePageObject<?> parentPageObject) {
        try {
            T instance = newInstance(elementClass, elementToWrap);
            instance.setName(name);

            // Add reference to pageobject / browser for those classes that want them
            setAwareValue(instance, parentPageObject);

            return instance;
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new HtmlElementsException(e);
        }
    }

    /**
     * Creates an instance of the given class representing a block of elements and initializes its fields
     * with lazy proxies.
     * <p>
     * Is a COPY of the Yandex HtmlElementLoader classes method so can replace
     * the call to populatePageObject() with one that supports our page decorator
     * AND set the page object if required.
     *
     * @param <T>              A class to be instantiated and initialized.
     * @param elementClass     A class to be instantiated and initialized.
     * @param elementToWrap    WebElement to wrap
     * @param name             Name of element
     * @param parentPageObject PageObject sitting on
     * @return Initialised instance of the specified class.
     */
    public static <T extends HtmlElement> T createHtmlElement(Class<T> elementClass, WebElement elementToWrap, String name, BasePageObject<?> parentPageObject) {
        try {
            T instance = newInstance(elementClass);
            instance.setWrappedElement(elementToWrap);
            instance.setName(name);

            // Add reference to pageobject / browser for those classes that want them
            setAwareValue(instance, parentPageObject);

            // Recursively initialize elements of the block
            populatePageObject(instance, elementToWrap, parentPageObject);
            return instance;
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new HtmlElementsException(e);
        }
    }

    /**
     * Initialises fields of the given HtmlElements object using specified locator factory.
     */
    private static void populatePageObject(Object instance, SearchContext searchContext, BasePageObject<?> parentPageObject) {
        PageFactory.initElements(new PageObjectAwareHtmlElementDecorator(new HtmlElementLocatorFactory(searchContext), parentPageObject), instance);
    }

    public static void setAwareValue(Object element, BasePageObject<?> parentPageObject) {
        if (element instanceof WebDriverAware) {
            ((WebDriverAware) element).setWebDriver(parentPageObject.getBrowser().getDriver());
        }

        if (element instanceof PageObjectAware) {
            ((PageObjectAware) element).setPageObject(parentPageObject);
        }
    }
}
