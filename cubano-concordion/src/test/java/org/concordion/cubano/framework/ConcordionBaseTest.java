package org.concordion.cubano.framework;

import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;

import org.concordion.cubano.framework.resource.CloseListener;
import org.concordion.cubano.framework.resource.ResourceScope;
import org.junit.Test;
import org.mockito.InOrder;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ConcordionBaseTest {

    private ConcordionBase test1 = new ConcordionBase() {};
    private List<String> closedResources = new ArrayList<>();
    private CloseListener listener = spy(CloseListener.class);

    private class TestResource implements Closeable {
        private final String name;

        private TestResource(String name) {
            this.name = name;
        }

        @Override
        public void close() throws IOException {
            closedResources.add(name);
        }
    }

    @Test
    public void closesResourcesInReverseOrder() {
        test1.registerCloseableResource(new TestResource("1"), ResourceScope.SPECIFICATION);
        test1.registerCloseableResource(new TestResource("2"), ResourceScope.SPECIFICATION);
        test1.registerCloseableResource(new TestResource("3"), ResourceScope.SPECIFICATION);

        test1.closeSpecificationResources();

        assertThat(closedResources, contains("3", "2", "1"));
    }

    @Test
    public void closesResourcesForSpecifiedScopeOnly() {
        test1.registerCloseableResource(new TestResource("0"), ResourceScope.EXAMPLE);
        test1.registerCloseableResource(new TestResource("1"), ResourceScope.SPECIFICATION);
        test1.registerCloseableResource(new TestResource("2"), ResourceScope.SUITE);
        test1.registerCloseableResource(new TestResource("3"), ResourceScope.SPECIFICATION);
        test1.registerCloseableResource(new TestResource("4"), ResourceScope.SUITE);
        test1.registerCloseableResource(new TestResource("5"), ResourceScope.EXAMPLE);

        test1.closeSpecificationResources();

        assertThat(closedResources, contains("3", "1"));
        test1.closeSuiteResources();
        assertThat(closedResources, contains("3", "1", "4", "2"));
    }

    @Test
    public void callsListenerWhenClosingExamples() {
        TestResource resource = new TestResource("8");
        test1.registerCloseableResource(resource, ResourceScope.EXAMPLE, listener);

        test1.closeExampleResources();

        verify(listener).beforeClosing(resource);
        verify(listener).afterClosing(resource);
    }

    @Test
    public void callsListenerWhenClosingSpecification() {
        TestResource resource = new TestResource("1");
        test1.registerCloseableResource(resource, ResourceScope.SPECIFICATION, listener);

        test1.closeSpecificationResources();

        verify(listener).beforeClosing(resource);
        verify(listener).afterClosing(resource);
    }

    @Test
    public void callsListenerWhenClosingMultiple() {
        TestResource resource1 = new TestResource("1");
        TestResource resource2 = new TestResource("2");
        TestResource resource3 = new TestResource("3");
        test1.registerCloseableResource(resource1, ResourceScope.EXAMPLE, listener);
        test1.registerCloseableResource(resource2, ResourceScope.SPECIFICATION, listener);
        test1.registerCloseableResource(resource3, ResourceScope.SUITE, listener);

        test1.closeExampleResources();
        test1.closeSpecificationResources();
        test1.closeSuiteResources();

        InOrder inOrder = inOrder(listener);
        inOrder.verify(listener).beforeClosing(resource1);
        inOrder.verify(listener).afterClosing(resource1);
        inOrder.verify(listener).beforeClosing(resource2);
        inOrder.verify(listener).afterClosing(resource2);
        inOrder.verify(listener).beforeClosing(resource3);
        inOrder.verify(listener).afterClosing(resource3);
        verifyNoMoreInteractions(listener);
    }

    @Test
    public void isRegisteredReturnsFalseIfNotRegistered() {
        TestResource testResource = new TestResource("1");

        assertFalse(test1.isRegistered(testResource, ResourceScope.SPECIFICATION));
    }

    @Test
    public void isRegisteredReturnsTrueIfRegistered() {
        TestResource testResource = new TestResource("1");
        test1.registerCloseableResource(testResource, ResourceScope.SPECIFICATION);

        assertTrue(test1.isRegistered(testResource, ResourceScope.SPECIFICATION));
    }
}
