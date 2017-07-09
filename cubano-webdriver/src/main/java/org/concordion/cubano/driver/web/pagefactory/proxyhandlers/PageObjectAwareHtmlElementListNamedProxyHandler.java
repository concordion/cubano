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

import ru.yandex.qatools.htmlelements.element.HtmlElement;

/**
 * A replacement for HtmlElementListNamedProxyHandler that implements PageObjectAware functionality.
 *
 * @param <T>
 * @author asumn001
 */
public class PageObjectAwareHtmlElementListNamedProxyHandler<T extends HtmlElement> implements InvocationHandler {

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
    public PageObjectAwareHtmlElementListNamedProxyHandler(Class<T> elementClass, ElementLocator locator, String name, BasePageObject<?> parentPageObject) {
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
            elements.add(PageObjectAwareHtmlElementsLoader.createHtmlElement(elementClass, element, newName, parentPageObject));
        }

        try {
            return method.invoke(elements, objects);
        } catch (InvocationTargetException e) {
            // Unwrap the underlying exception
            throw e.getCause();
        }
    }
}
