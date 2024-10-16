package io.cucumber.spring;

import io.cucumber.core.backend.CucumberBackendException;
import io.cucumber.spring.beans.BellyBean;
import io.cucumber.spring.beans.DummyComponent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.TestContextManager;
import org.springframework.test.context.TestExecutionListener;

import java.util.ArrayList;
import java.util.List;

import static io.cucumber.spring.TestContextAdaptor.create;
import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TestContextAdaptorTest {

    @Test
    void invokesAllLiveCycleHooks() {
        MockTestExecutionListener listener = new MockTestExecutionListener();
        TestContextManager manager = new TestContextManager(SomeContextConfiguration.class);
        TestContextAdaptor adaptor = create(() -> manager, singletonList(SomeContextConfiguration.class));
        manager.registerTestExecutionListeners(listener);

        adaptor.start();
        assertIterableEquals(List.of(
            "beforeTestClass",
            "prepareTestInstance",
            "beforeTestMethod",
            "beforeTestExecution"),
            listener.events);

        listener.events.clear();
        adaptor.stop();
        assertIterableEquals(List.of(
            "afterTestExecution",
            "afterTestMethod",
            "afterTestClass"),
            listener.events);
    }

    @Test
    void invokesAfterClassIfBeforeClassFailed() {
        MockTestExecutionListener listener = new MockTestExecutionListener("beforeTestClass");
        TestContextManager manager = new TestContextManager(SomeContextConfiguration.class);
        TestContextAdaptor adaptor = create(() -> manager, singletonList(SomeContextConfiguration.class));
        manager.registerTestExecutionListeners(listener);

        assertThrows(CucumberBackendException.class, adaptor::start);
        assertIterableEquals(List.of(
            "beforeTestClass"),
            listener.events);

        listener.events.clear();
        adaptor.stop();
        assertIterableEquals(List.of(
            "afterTestClass"),
            listener.events);
    }

    @Test
    void invokesAfterClassIfPrepareTestInstanceFailed() {
        MockTestExecutionListener listener = new MockTestExecutionListener("prepareTestInstance");
        TestContextManager manager = new TestContextManager(SomeContextConfiguration.class);
        TestContextAdaptor adaptor = create(() -> manager, singletonList(SomeContextConfiguration.class));
        manager.registerTestExecutionListeners(listener);

        assertThrows(CucumberBackendException.class, adaptor::start);
        assertIterableEquals(List.of(
            "beforeTestClass",
            "prepareTestInstance"),
            listener.events);

        listener.events.clear();
        adaptor.stop();
        assertIterableEquals(List.of(
            "afterTestClass"),
            listener.events);
    }

    @Test
    void invokesAfterMethodIfBeforeMethodThrows() {
        MockTestExecutionListener listener = new MockTestExecutionListener("beforeTestMethod");
        TestContextManager manager = new TestContextManager(SomeContextConfiguration.class);
        TestContextAdaptor adaptor = create(() -> manager, singletonList(SomeContextConfiguration.class));
        manager.registerTestExecutionListeners(listener);

        assertThrows(CucumberBackendException.class, adaptor::start);
        assertIterableEquals(List.of(
            "beforeTestClass",
            "prepareTestInstance",
            "beforeTestMethod"),
            listener.events);

        listener.events.clear();
        adaptor.stop();
        assertIterableEquals(List.of(
            "afterTestMethod",
            "afterTestClass"),
            listener.events);
    }

    @Test
    void invokesAfterTestExecutionIfBeforeTestExecutionThrows() {
        MockTestExecutionListener listener = new MockTestExecutionListener("beforeTestExecution");
        TestContextManager manager = new TestContextManager(SomeContextConfiguration.class);
        TestContextAdaptor adaptor = create(() -> manager, singletonList(SomeContextConfiguration.class));
        manager.registerTestExecutionListeners(listener);

        assertThrows(CucumberBackendException.class, adaptor::start);
        assertIterableEquals(List.of(
            "beforeTestClass",
            "prepareTestInstance",
            "beforeTestMethod",
            "beforeTestExecution"),
            listener.events);

        listener.events.clear();
        adaptor.stop();
        assertIterableEquals(List.of(
            "afterTestExecution",
            "afterTestMethod",
            "afterTestClass"),
            listener.events);
    }

    @Test
    void invokesAfterTestMethodIfAfterTestExecutionThrows() {
        MockTestExecutionListener listener = new MockTestExecutionListener("afterTestExecution");
        TestContextManager manager = new TestContextManager(SomeContextConfiguration.class);
        TestContextAdaptor adaptor = create(() -> manager, singletonList(SomeContextConfiguration.class));
        manager.registerTestExecutionListeners(listener);

        adaptor.start();
        assertIterableEquals(List.of(
            "beforeTestClass",
            "prepareTestInstance",
            "beforeTestMethod",
            "beforeTestExecution"),
            listener.events);

        listener.events.clear();
        assertThrows(CucumberBackendException.class, adaptor::stop);
        assertIterableEquals(List.of(
            "afterTestExecution",
            "afterTestMethod",
            "afterTestClass"),
            listener.events);
    }

    @Test
    void invokesAfterTesClassIfAfterTestMethodThrows() throws Exception {
        MockTestExecutionListener listener = new MockTestExecutionListener("afterTestMethod");
        TestContextManager manager = new TestContextManager(SomeContextConfiguration.class);
        TestContextAdaptor adaptor = create(() -> manager, singletonList(SomeContextConfiguration.class));
        manager.registerTestExecutionListeners(listener);

        adaptor.start();
        assertIterableEquals(List.of(
            "beforeTestClass",
            "prepareTestInstance",
            "beforeTestMethod",
            "beforeTestExecution"),
            listener.events);

        listener.events.clear();
        assertThrows(CucumberBackendException.class, adaptor::stop);
        assertIterableEquals(List.of(
            "afterTestExecution",
            "afterTestMethod",
            "afterTestClass"),
            listener.events);
    }

    @Test
    void invokesAllMethodsPriorIfAfterTestClassThrows() {
        MockTestExecutionListener listener = new MockTestExecutionListener("afterTestExecution");
        TestContextManager manager = new TestContextManager(SomeContextConfiguration.class);
        TestContextAdaptor adaptor = create(() -> manager, singletonList(SomeContextConfiguration.class));
        manager.registerTestExecutionListeners(listener);

        adaptor.start();
        assertIterableEquals(List.of(
            "beforeTestClass",
            "prepareTestInstance",
            "beforeTestMethod",
            "beforeTestExecution"),
            listener.events);

        listener.events.clear();
        assertThrows(CucumberBackendException.class, adaptor::stop);
        assertIterableEquals(List.of(
            "afterTestExecution",
            "afterTestMethod",
            "afterTestClass"),
            listener.events);
    }

    @ParameterizedTest
    @ValueSource(classes = { WithAutowiredDependency.class, WithConstructorDependency.class })
    void autowireAndPostProcessesOnlyOnce(Class<? extends Spy> testClass) {
        TestContextManager manager = new TestContextManager(testClass);
        TestContextAdaptor adaptor = create(() -> manager, singletonList(testClass));

        assertAll(
            () -> assertDoesNotThrow(adaptor::start),
            () -> assertNotNull(manager.getTestContext().getTestInstance()),
            () -> assertSame(manager.getTestContext().getTestInstance(), adaptor.getInstance(testClass)),
            () -> assertEquals(1, adaptor.getInstance(testClass).autowiredCount()),
            () -> assertEquals(1, adaptor.getInstance(testClass).postProcessedCount()),
            () -> assertNotNull(adaptor.getInstance(testClass).getBelly()),
            () -> assertNotNull(adaptor.getInstance(testClass).getDummyComponent()),
            () -> assertDoesNotThrow(adaptor::stop));
    }

    @CucumberContextConfiguration
    @ContextConfiguration("classpath:cucumber.xml")
    public static class SomeContextConfiguration {

    }

    private interface Spy {

        int postProcessedCount();

        int autowiredCount();

        BellyBean getBelly();

        DummyComponent getDummyComponent();

    }

    @CucumberContextConfiguration
    @ContextConfiguration("classpath:cucumber.xml")
    public static class WithAutowiredDependency implements BeanNameAware, Spy {

        @Autowired
        BellyBean belly;

        int postProcessedCount = 0;
        int autowiredCount = 0;

        private DummyComponent dummyComponent;

        @Autowired
        public void setDummyComponent(DummyComponent dummyComponent) {
            this.dummyComponent = dummyComponent;
            this.autowiredCount++;
        }

        @Override
        public void setBeanName(@NonNull String ignored) {
            postProcessedCount++;
        }

        @Override
        public int postProcessedCount() {
            return postProcessedCount;
        }

        @Override
        public int autowiredCount() {
            return autowiredCount;
        }

        @Override
        public BellyBean getBelly() {
            return belly;
        }

        @Override
        public DummyComponent getDummyComponent() {
            return dummyComponent;
        }
    }

    @CucumberContextConfiguration
    @ContextConfiguration("classpath:cucumber.xml")
    public static class WithConstructorDependency implements BeanNameAware, Spy {

        final BellyBean belly;
        final DummyComponent dummyComponent;

        int postProcessedCount = 0;
        int autowiredCount = 0;

        public WithConstructorDependency(BellyBean belly, DummyComponent dummyComponent) {
            this.belly = belly;
            this.dummyComponent = dummyComponent;
            this.autowiredCount++;
        }

        @Override
        public void setBeanName(@NonNull String ignored) {
            postProcessedCount++;
        }

        @Override
        public int postProcessedCount() {
            return postProcessedCount;
        }

        @Override
        public int autowiredCount() {
            return autowiredCount;
        }

        @Override
        public BellyBean getBelly() {
            return belly;
        }

        @Override
        public DummyComponent getDummyComponent() {
            return dummyComponent;
        }
    }

    private static class MockTestExecutionListener implements TestExecutionListener {
        private final List<String> eventsThrowingExceptions;
        List<String> events = new ArrayList<>();

        public MockTestExecutionListener(String... eventsThrowingExceptions) {
            this.eventsThrowingExceptions = List.of(eventsThrowingExceptions);
        }

        private void addEvent(String eventName) {
            events.add(eventName);
            if (eventsThrowingExceptions.contains(eventName)) {
                throw new RuntimeException(eventName);
            }
        }

        @Override
        public void beforeTestClass(TestContext testContext) {
            addEvent("beforeTestClass");
        }

        @Override
        public void prepareTestInstance(TestContext testContext) {
            addEvent("prepareTestInstance");
        }

        @Override
        public void beforeTestMethod(TestContext testContext) {
            addEvent("beforeTestMethod");
        }

        @Override
        public void beforeTestExecution(TestContext testContext) {
            addEvent("beforeTestExecution");
        }

        @Override
        public void afterTestExecution(TestContext testContext) {
            addEvent("afterTestExecution");
        }

        @Override
        public void afterTestMethod(TestContext testContext) {
            addEvent("afterTestMethod");
        }

        @Override
        public void afterTestClass(TestContext testContext) {
            addEvent("afterTestClass");
        }
    }
}
