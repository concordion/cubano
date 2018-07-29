package org.concordion.cubano.framework;

import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;
import org.junit.Test;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ConcordionBaseTest {

    private ConcordionBase test1 = new ConcordionBase() {};
    private List<String> closedResources = new ArrayList<>();

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
        test1.registerCloseableResource(new TestResource("1"), ResourceScope.SPECIFICATION);
        test1.registerCloseableResource(new TestResource("2"), ResourceScope.SUITE);
        test1.registerCloseableResource(new TestResource("3"), ResourceScope.SPECIFICATION);
        test1.registerCloseableResource(new TestResource("4"), ResourceScope.SUITE);
        test1.closeSpecificationResources();
        assertThat(closedResources, contains("3", "1"));
        test1.closeSuiteResources();
        assertThat(closedResources, contains("3", "1", "4", "2"));
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
