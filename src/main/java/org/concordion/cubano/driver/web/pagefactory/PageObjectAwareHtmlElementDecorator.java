package org.concordion.cubano.driver.web.pagefactory;

import static ru.yandex.qatools.htmlelements.loader.decorator.ProxyFactory.createHtmlElementListProxy;
import static ru.yandex.qatools.htmlelements.loader.decorator.ProxyFactory.createTypifiedElementListProxy;
import static ru.yandex.qatools.htmlelements.utils.HtmlElementUtils.getElementName;
import static ru.yandex.qatools.htmlelements.utils.HtmlElementUtils.getGenericParameterClass;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.util.List;

import org.concordion.cubano.driver.web.BasePageObject;
import org.concordion.cubano.driver.web.pagefactory.proxyhandlers.PageObjectAwareHtmlElementListNamedProxyHandler;
import org.concordion.cubano.driver.web.pagefactory.proxyhandlers.PageObjectAwareTypifiedElementListNamedProxyHandler;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.pagefactory.ElementLocator;

import ru.yandex.qatools.htmlelements.element.HtmlElement;
import ru.yandex.qatools.htmlelements.element.TypifiedElement;
import ru.yandex.qatools.htmlelements.loader.decorator.HtmlElementDecorator;
import ru.yandex.qatools.htmlelements.pagefactory.CustomElementLocatorFactory;
import ru.yandex.qatools.htmlelements.utils.HtmlElementUtils;

/**
 * A custom implementation of the {@link HtmlElementDecorator} that supports the
 * {@link WebDriverAware} and {@link PageObjectAware} interfaces.
 *
 * @author Andrew Sumner
 */
public class PageObjectAwareHtmlElementDecorator extends HtmlElementDecorator {
    private BasePageObject<?> pageObject;

    /**
     * Constructor.
     *
     * @param factory    Element locator factory
     * @param pageObject PageObject being decorated
     */
    public PageObjectAwareHtmlElementDecorator(CustomElementLocatorFactory factory, BasePageObject<?> pageObject) {
        super(factory);

        this.pageObject = pageObject;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected <T extends TypifiedElement> T decorateTypifiedElement(ClassLoader loader, Field field) {
        WebElement elementToWrap = decorateWebElement(loader, field);
        String name = getElementName(field);

        // Calling our custom createTypifiedElement method rather than the one supplied by Yandex
        return PageObjectAwareHtmlElementsLoader.createTypifiedElement((Class<T>) field.getType(), elementToWrap, name, pageObject);
    }

    @Override
    protected <T extends TypifiedElement> List<T> decorateTypifiedElementList(ClassLoader loader, Field field) {
        @SuppressWarnings("unchecked")
        Class<T> elementClass = (Class<T>) getGenericParameterClass(field);
        ElementLocator locator = factory.createLocator(field);
        String name = getElementName(field);

        // Using our custom proxy handler rather than the one supplied by Yandex
        InvocationHandler handler = new PageObjectAwareTypifiedElementListNamedProxyHandler<>(elementClass, locator, name, pageObject);

        return createTypifiedElementListProxy(loader, handler);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected <T extends HtmlElement> T decorateHtmlElement(ClassLoader loader, Field field) {
        WebElement elementToWrap = decorateWebElement(loader, field);

        // Calling our custom createHtmlElement method rather than the one supplied by Yandex
        return PageObjectAwareHtmlElementsLoader.createHtmlElement((Class<T>) field.getType(), elementToWrap, HtmlElementUtils.getElementName(field), pageObject);
    }

    @Override
    protected <T extends HtmlElement> List<T> decorateHtmlElementList(ClassLoader loader, Field field) {
        @SuppressWarnings("unchecked")
        Class<T> elementClass = (Class<T>) getGenericParameterClass(field);
        ElementLocator locator = factory.createLocator(field);
        String name = getElementName(field);

        // Using our custom proxy handler rather than the one supplied by Yandex
        InvocationHandler handler = new PageObjectAwareHtmlElementListNamedProxyHandler<>(elementClass, locator, name, pageObject);

        return createHtmlElementListProxy(loader, handler);
    }
}
