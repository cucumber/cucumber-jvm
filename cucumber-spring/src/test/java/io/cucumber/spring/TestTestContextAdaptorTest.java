package io.cucumber.spring;

import io.cucumber.core.backend.CucumberBackendException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestContextManager;
import org.springframework.test.context.TestExecutionListener;

import static java.util.Collections.singletonList;
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
        TestContextAdaptor adaptor = new TestContextAdaptor(manager, singletonList(SomeContextConfiguration.class));
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
        TestContextAdaptor adaptor = new TestContextAdaptor(manager, singletonList(SomeContextConfiguration.class));
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
        TestContextAdaptor adaptor = new TestContextAdaptor(manager, singletonList(SomeContextConfiguration.class));
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
        TestContextAdaptor adaptor = new TestContextAdaptor(manager, singletonList(SomeContextConfiguration.class));
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
        TestContextAdaptor adaptor = new TestContextAdaptor(manager, singletonList(SomeContextConfiguration.class));
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
        TestContextAdaptor adaptor = new TestContextAdaptor(manager, singletonList(SomeContextConfiguration.class));
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
        TestContextAdaptor adaptor = new TestContextAdaptor(manager, singletonList(SomeContextConfiguration.class));
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
        TestContextAdaptor adaptor = new TestContextAdaptor(manager, singletonList(SomeContextConfiguration.class));
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

    @CucumberContextConfiguration
    @ContextConfiguration("classpath:cucumber.xml")
    public static class SomeContextConfiguration {

    }

}
