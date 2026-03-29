package io.cucumber.java8;

import io.cucumber.core.backend.Glue;
import io.cucumber.core.backend.GlueDiscoveryRequest;
import io.cucumber.core.backend.ObjectFactory;
import io.cucumber.java8.steps.Steps;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoSettings;

import java.net.URI;
import java.util.List;

import static java.lang.Thread.currentThread;
import static java.util.Collections.emptyList;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@MockitoSettings
class Java8BackendTest {

    @Mock
    private Glue glue;

    @Mock
    private ObjectFactory factory;

    private Java8Backend backend;

    @BeforeEach
    void createBackend() {
        this.backend = new Java8Backend(factory, factory, currentThread()::getContextClassLoader);
    }

    @Test
    void finds_step_definitions_by_classpath_url() {
        TestGlueDiscoveryRequest glueDiscoveryRequest = new TestGlueDiscoveryRequest(
            URI.create("classpath:io/cucumber/java8/steps"));
        backend.loadGlue(glue, glueDiscoveryRequest);
        backend.buildWorld();
        verify(factory).addClass(Steps.class);
    }

    @Test
    void finds_step_definitions_by_class_name() {
        TestGlueDiscoveryRequest glueDiscoveryRequest = new TestGlueDiscoveryRequest(Steps.class.getName());
        backend.loadGlue(glue, glueDiscoveryRequest);
        backend.buildWorld();
        verify(factory).addClass(Steps.class);
    }

    @Test
    void finds_step_definitions_once_by_classpath_url() {
        TestGlueDiscoveryRequest glueDiscoveryRequest = new TestGlueDiscoveryRequest(
            URI.create("classpath:io/cucumber/java8/steps"),
            URI.create("classpath:io/cucumber/java8/steps"));
        backend.loadGlue(glue, glueDiscoveryRequest);
        backend.buildWorld();
        verify(factory, times(1)).addClass(Steps.class);
    }

    private static final class TestGlueDiscoveryRequest implements GlueDiscoveryRequest {
        private final List<URI> gluePaths;
        private final List<String> glueClassNames;

        TestGlueDiscoveryRequest(URI... gluePaths) {
            this.gluePaths = List.of(gluePaths);
            this.glueClassNames = emptyList();
        }

        TestGlueDiscoveryRequest(String... glueClassNames) {
            this.gluePaths = emptyList();
            this.glueClassNames = List.of(glueClassNames);
        }

        @Override
        public List<URI> getGlue() {
            return gluePaths;
        }

        @Override
        public List<String> getGlueClassNames() {
            return glueClassNames;
        }
    }
}
