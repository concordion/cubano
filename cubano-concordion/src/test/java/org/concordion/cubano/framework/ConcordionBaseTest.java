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

    private final TestResource resource0 = new TestResource("0");
    private final TestResource resource1 = new TestResource("1");
    private final TestResource resource2 = new TestResource("2");
    private final TestResource resource3 = new TestResource("3");
    private final TestResource resource4 = new TestResource("4");
    private final TestResource resource5 = new TestResource("5");
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
    public void isRegisteredReturnsFalseIfNotRegistered() {
        assertFalse(test1.isRegistered(resource0, ResourceScope.SPECIFICATION));
    }

    @Test
    public void isRegisteredReturnsTrueIfRegistered() {
        test1.registerCloseableResource(resource0, ResourceScope.SPECIFICATION);

        assertTrue(test1.isRegistered(resource0, ResourceScope.SPECIFICATION));
    }

    @Test
    public void resourcesAreRegisteredToCorrectScope() {
        test1.registerCloseableResource(resource0, ResourceScope.EXAMPLE);
        test1.registerCloseableResource(resource1, ResourceScope.SPECIFICATION);
        test1.registerCloseableResource(resource2, ResourceScope.SUITE);

        assertTrue(test1.isRegistered(resource0, ResourceScope.EXAMPLE));
        assertTrue(test1.isRegistered(resource1, ResourceScope.SPECIFICATION));
        assertTrue(test1.isRegistered(resource2, ResourceScope.SUITE));
    }

    @Test
    public void resourcesAreNotRegisteredToIncorrectScope() {
        test1.registerCloseableResource(resource0, ResourceScope.EXAMPLE);

        assertTrue(test1.isRegistered(resource0, ResourceScope.EXAMPLE));
        assertFalse(test1.isRegistered(resource0, ResourceScope.SPECIFICATION));
        assertFalse(test1.isRegistered(resource0, ResourceScope.SUITE));
    }

    @Test
    public void closesSpecifiedResourcesOnly() {
        test1.registerCloseableResource(resource0, ResourceScope.EXAMPLE);
        test1.registerCloseableResource(resource1, ResourceScope.SPECIFICATION);
        test1.registerCloseableResource(resource2, ResourceScope.SUITE);
        test1.registerCloseableResource(resource3, ResourceScope.SPECIFICATION);
        test1.registerCloseableResource(resource4, ResourceScope.SPECIFICATION);
        test1.registerCloseableResource(resource5, ResourceScope.EXAMPLE);

        test1.closeResource(resource3);
        test1.closeResource(resource5);

        assertThat(closedResources, contains("3", "5"));
    }

    @Test
    public void closesResourcesInReverseOrder() {
        test1.registerCloseableResource(resource1, ResourceScope.SPECIFICATION);
        test1.registerCloseableResource(resource2, ResourceScope.SPECIFICATION);
        test1.registerCloseableResource(resource3, ResourceScope.SPECIFICATION);

        test1.closeSpecificationResources();

        assertThat(closedResources, contains("3", "2", "1"));
    }

    @Test
    public void closesResourcesForSpecifiedScopeOnly() {
        test1.registerCloseableResource(resource0, ResourceScope.EXAMPLE);
        test1.registerCloseableResource(resource1, ResourceScope.SPECIFICATION);
        test1.registerCloseableResource(resource2, ResourceScope.SUITE);
        test1.registerCloseableResource(resource3, ResourceScope.SPECIFICATION);
        test1.registerCloseableResource(resource4, ResourceScope.SUITE);
        test1.registerCloseableResource(resource5, ResourceScope.EXAMPLE);

        test1.closeSpecificationResources();

        assertThat(closedResources, contains("3", "1"));
        test1.closeSuiteResources();
        assertThat(closedResources, contains("3", "1", "4", "2"));
    }

    @Test
    public void callsListenerWhenClosingExamples() {
        test1.registerCloseableResource(resource2, ResourceScope.EXAMPLE, listener);

        test1.closeExampleResources();

        verify(listener).beforeClosing(resource2);
        verify(listener).afterClosing(resource2);
    }

    @Test
    public void callsListenerWhenClosingSpecification() {
        test1.registerCloseableResource(resource1, ResourceScope.SPECIFICATION, listener);

        test1.closeSpecificationResources();

        verify(listener).beforeClosing(resource1);
        verify(listener).afterClosing(resource1);
    }

    @Test
    public void callsListenerWhenClosingMultiple() {
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
}
