package io.cucumber.spring;

import io.cucumber.core.backend.CucumberBackendException;
import io.cucumber.spring.beans.BellyBean;
import io.cucumber.spring.beans.DummyComponent;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestContextManager;
import org.springframework.test.context.TestExecutionListener;

import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;

@ExtendWith(MockitoExtension.class)
public class TestTestContextAdaptorTest {

    @Mock
    TestExecutionListener listener;

    @AfterEach
    void verifyNoMoroInteractions() {
        Mockito.verifyNoMoreInteractions(listener);
    }

    @Test
    void invokesAllLiveCycleHooks() throws Exception {
        TestContextManager manager = new TestContextManager(SomeContextConfiguration.class);
        TestContextAdaptor adaptor = new TestContextAdaptor(() -> manager,
            singletonList(SomeContextConfiguration.class));
        manager.registerTestExecutionListeners(listener);
        InOrder inOrder = inOrder(listener);

        adaptor.start();
        inOrder.verify(listener).beforeTestClass(any());
        inOrder.verify(listener).prepareTestInstance(any());
        inOrder.verify(listener).beforeTestMethod(any());
        inOrder.verify(listener).beforeTestExecution(any());

        adaptor.stop();
        inOrder.verify(listener).afterTestExecution(any());
        inOrder.verify(listener).afterTestMethod(any());
        inOrder.verify(listener).afterTestClass(any());
    }

    @Test
    void invokesAfterClassIfBeforeClassFailed() throws Exception {
        TestContextManager manager = new TestContextManager(SomeContextConfiguration.class);
        TestContextAdaptor adaptor = new TestContextAdaptor(() -> manager,
            singletonList(SomeContextConfiguration.class));
        manager.registerTestExecutionListeners(listener);
        InOrder inOrder = inOrder(listener);

        doThrow(new RuntimeException()).when(listener).beforeTestClass(any());

        assertThrows(CucumberBackendException.class, adaptor::start);
        inOrder.verify(listener).beforeTestClass(any());

        adaptor.stop();
        inOrder.verify(listener).afterTestClass(any());
    }

    @Test
    void invokesAfterClassIfPrepareTestInstanceFailed() throws Exception {
        TestContextManager manager = new TestContextManager(SomeContextConfiguration.class);
        TestContextAdaptor adaptor = new TestContextAdaptor(() -> manager,
            singletonList(SomeContextConfiguration.class));
        manager.registerTestExecutionListeners(listener);
        InOrder inOrder = inOrder(listener);

        doThrow(new RuntimeException()).when(listener).prepareTestInstance(any());

        assertThrows(CucumberBackendException.class, adaptor::start);
        inOrder.verify(listener).beforeTestClass(any());

        adaptor.stop();
        inOrder.verify(listener).afterTestClass(any());
    }

    @Test
    void invokesAfterMethodIfBeforeMethodThrows() throws Exception {
        TestContextManager manager = new TestContextManager(SomeContextConfiguration.class);
        TestContextAdaptor adaptor = new TestContextAdaptor(() -> manager,
            singletonList(SomeContextConfiguration.class));
        manager.registerTestExecutionListeners(listener);
        InOrder inOrder = inOrder(listener);

        doThrow(new RuntimeException()).when(listener).beforeTestMethod(any());

        assertThrows(CucumberBackendException.class, adaptor::start);
        inOrder.verify(listener).beforeTestClass(any());
        inOrder.verify(listener).prepareTestInstance(any());
        inOrder.verify(listener).beforeTestMethod(any());

        adaptor.stop();
        inOrder.verify(listener).afterTestMethod(any());
        inOrder.verify(listener).afterTestClass(any());
    }

    @Test
    void invokesAfterTestExecutionIfBeforeTestExecutionThrows() throws Exception {
        TestContextManager manager = new TestContextManager(SomeContextConfiguration.class);
        TestContextAdaptor adaptor = new TestContextAdaptor(() -> manager,
            singletonList(SomeContextConfiguration.class));
        manager.registerTestExecutionListeners(listener);
        InOrder inOrder = inOrder(listener);

        doThrow(new RuntimeException()).when(listener).beforeTestExecution(any());

        assertThrows(CucumberBackendException.class, adaptor::start);
        inOrder.verify(listener).beforeTestClass(any());
        inOrder.verify(listener).prepareTestInstance(any());
        inOrder.verify(listener).beforeTestMethod(any());

        adaptor.stop();
        inOrder.verify(listener).afterTestExecution(any());
        inOrder.verify(listener).afterTestMethod(any());
        inOrder.verify(listener).afterTestClass(any());
    }

    @Test
    void invokesAfterTestMethodIfAfterTestExecutionThrows() throws Exception {
        TestContextManager manager = new TestContextManager(SomeContextConfiguration.class);
        TestContextAdaptor adaptor = new TestContextAdaptor(() -> manager,
            singletonList(SomeContextConfiguration.class));
        manager.registerTestExecutionListeners(listener);
        InOrder inOrder = inOrder(listener);

        doThrow(new RuntimeException()).when(listener).afterTestExecution(any());

        adaptor.start();
        inOrder.verify(listener).beforeTestClass(any());
        inOrder.verify(listener).prepareTestInstance(any());
        inOrder.verify(listener).beforeTestMethod(any());
        inOrder.verify(listener).beforeTestExecution(any());

        assertThrows(CucumberBackendException.class, adaptor::stop);
        inOrder.verify(listener).afterTestExecution(any());
        inOrder.verify(listener).afterTestMethod(any());
        inOrder.verify(listener).afterTestClass(any());
    }

    @Test
    void invokesAfterTesClassIfAfterTestMethodThrows() throws Exception {
        TestContextManager manager = new TestContextManager(SomeContextConfiguration.class);
        TestContextAdaptor adaptor = new TestContextAdaptor(() -> manager,
            singletonList(SomeContextConfiguration.class));
        manager.registerTestExecutionListeners(listener);
        InOrder inOrder = inOrder(listener);

        doThrow(new RuntimeException()).when(listener).afterTestMethod(any());

        adaptor.start();
        inOrder.verify(listener).beforeTestClass(any());
        inOrder.verify(listener).prepareTestInstance(any());
        inOrder.verify(listener).beforeTestMethod(any());
        inOrder.verify(listener).beforeTestExecution(any());

        assertThrows(CucumberBackendException.class, adaptor::stop);
        inOrder.verify(listener).afterTestExecution(any());
        inOrder.verify(listener).afterTestMethod(any());
        inOrder.verify(listener).afterTestClass(any());
    }

    @Test
    void invokesAllMethodsPriorIfAfterTestClassThrows() throws Exception {
        TestContextManager manager = new TestContextManager(SomeContextConfiguration.class);
        TestContextAdaptor adaptor = new TestContextAdaptor(() -> manager,
            singletonList(SomeContextConfiguration.class));
        manager.registerTestExecutionListeners(listener);
        InOrder inOrder = inOrder(listener);

        doThrow(new RuntimeException()).when(listener).afterTestExecution(any());

        adaptor.start();
        inOrder.verify(listener).beforeTestClass(any());
        inOrder.verify(listener).prepareTestInstance(any());
        inOrder.verify(listener).beforeTestMethod(any());
        inOrder.verify(listener).beforeTestExecution(any());

        assertThrows(CucumberBackendException.class, adaptor::stop);
        inOrder.verify(listener).afterTestExecution(any());
        inOrder.verify(listener).afterTestMethod(any());
        inOrder.verify(listener).afterTestClass(any());
    }

    @ParameterizedTest
    @ValueSource(classes = { WithAutowiredDependency.class, WithConstructorDependency.class })
    void autowireAndPostProcessesOnlyOnce(Class<? extends Spy> testClass) {
        TestContextManager manager = new TestContextManager(testClass);
        TestContextAdaptor adaptor = new TestContextAdaptor(() -> manager, singletonList(testClass));

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

}
