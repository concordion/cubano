package org.concordion.cubano.driver.web.pagefactory.proxyhandlers;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;

import org.concordion.cubano.driver.web.BasePageObject;
import org.concordion.cubano.driver.web.pagefactory.PageObjectAwareHtmlElementsLoader;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.pagefactory.ElementLocator;

import ru.yandex.qatools.htmlelements.element.TypifiedElement;

/**
 * A replacement for TypifiedElementListNamedProxyHandle that implements
 * PageObjectAware functionality.
 *
 * @param <T>
 *            Extension of TypifiedElement.
 * @author asumn001
 */
public class PageObjectAwareTypifiedElementListNamedProxyHandler<T extends TypifiedElement> implements InvocationHandler {
    private final Class<T> elementClass;
    private final ElementLocator locator;
    private final String name;
    private final BasePageObject<?> parentPageObject;

    /**
     * Constructor.
     *
     * @param elementClass     Class of element
     * @param locator          Locator for element
     * @param name             Name of element
     * @param parentPageObject Parent pageObject containing element
     */
    public PageObjectAwareTypifiedElementListNamedProxyHandler(Class<T> elementClass, ElementLocator locator, String name, BasePageObject<?> parentPageObject) {
        this.elementClass = elementClass;
        this.locator = locator;
        this.name = name;
        this.parentPageObject = parentPageObject;
    }

    @Override
    public Object invoke(Object o, Method method, Object[] objects) throws Throwable {
        if ("toString".equals(method.getName())) {
            return name;
        }

        List<T> elements = new LinkedList<>();
        int elementNumber = 0;
        for (WebElement element : locator.findElements()) {
            String newName = String.format("%s [%d]", name, elementNumber++);
            elements.add(PageObjectAwareHtmlElementsLoader.createTypifiedElement(elementClass, element, newName, parentPageObject));
        }

        try {
            return method.invoke(elements, objects);
        } catch (InvocationTargetException e) {
            // Unwrap the underlying exception
            throw e.getCause();
        }
    }
}

