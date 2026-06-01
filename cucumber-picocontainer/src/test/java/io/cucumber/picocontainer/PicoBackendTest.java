package io.cucumber.picocontainer;

import io.cucumber.core.backend.Glue;
import io.cucumber.core.backend.GlueDiscoveryRequest;
import io.cucumber.core.backend.ObjectFactory;
import io.cucumber.picocontainer.annotationconfig.DatabaseConnectionProvider;
import io.cucumber.picocontainer.annotationconfig.ExamplePicoConfiguration;
import io.cucumber.picocontainer.annotationconfig.UrlToUriProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.net.URI;
import java.util.List;

import static java.lang.Thread.currentThread;
import static java.util.Collections.emptyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
final class PicoBackendTest {

    @Mock
    private Glue glue;

    @Mock
    private ObjectFactory factory;

    private PicoBackend backend;

    @BeforeEach
    void createBackend() {
        this.backend = new PicoBackend(this.factory, currentThread()::getContextClassLoader);
    }

    @Test
    void considers_but_does_not_add_annotated_configuration() {
        backend.loadGlue(glue,
            new TestGlueDiscoveryRequest(URI.create("classpath:io/cucumber/picocontainer/annotationconfig")));
        backend.buildWorld();
        verify(factory, never()).addClass(ExamplePicoConfiguration.class);
    }

    @Test
    void adds_unnested_provider_classes() {
        backend.loadGlue(glue,
            new TestGlueDiscoveryRequest(URI.create("classpath:io/cucumber/picocontainer/annotationconfig")));
        backend.buildWorld();
        verify(factory).addClass(UrlToUriProvider.class);
        verify(factory).addClass(DatabaseConnectionProvider.class);
    }

    @Test
    void adds_unnested_provider_classes_from_class_name() {
        backend.loadGlue(glue,
            new TestGlueDiscoveryRequest(UrlToUriProvider.class.getName()));
        backend.buildWorld();
        verify(factory).addClass(UrlToUriProvider.class);
    }

    @Test
    void adds_nested_provider_classes() {
        backend.loadGlue(glue,
            new TestGlueDiscoveryRequest(URI.create("classpath:io/cucumber/picocontainer/annotationconfig")));
        backend.buildWorld();
        verify(factory).addClass(ExamplePicoConfiguration.NestedUrlProvider.class);
        verify(factory).addClass(ExamplePicoConfiguration.NestedUrlConnectionProvider.class);
    }

    @Test
    void finds_configured_classes_only_once_when_scanning_twice() {
        backend.loadGlue(glue, new TestGlueDiscoveryRequest(
            URI.create("classpath:io/cucumber/picocontainer/annotationconfig"),
            URI.create("classpath:io/cucumber/picocontainer/annotationconfig")));
        backend.buildWorld();
        verify(factory, never()).addClass(ExamplePicoConfiguration.class);
        verify(factory, times(1)).addClass(ExamplePicoConfiguration.NestedUrlProvider.class);
        verify(factory, times(1)).addClass(ExamplePicoConfiguration.NestedUrlConnectionProvider.class);
        verify(factory, times(1)).addClass(UrlToUriProvider.class);
        verify(factory, times(1)).addClass(DatabaseConnectionProvider.class);
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
