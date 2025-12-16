package io.cucumber.picocontainer;

import io.cucumber.core.backend.Glue;
import io.cucumber.core.backend.ObjectFactory;
import io.cucumber.picocontainer.annotationconfig.DatabaseConnectionProvider;
import io.cucumber.picocontainer.annotationconfig.ExamplePicoConfiguration;
import io.cucumber.picocontainer.annotationconfig.URLConnectionProvider;
import io.cucumber.picocontainer.annotationconfig.UrlToUriProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.net.URI;

import static java.lang.Thread.currentThread;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PicoBackendTest {

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
            singletonList(URI.create("classpath:io/cucumber/picocontainer/annotationconfig")));
        backend.buildWorld();
        verify(factory, never()).addClass(ExamplePicoConfiguration.class);
    }

    @Test
    void adds_referenced_provider_classes() {
        backend.loadGlue(glue,
            singletonList(URI.create("classpath:io/cucumber/picocontainer/annotationconfig")));
        backend.buildWorld();
        verify(factory).addClass(URLConnectionProvider.class);
        verify(factory).addClass(DatabaseConnectionProvider.class);
    }

    @Test
    void adds_selfsufficient_provider_classes() {
        backend.loadGlue(glue,
            singletonList(URI.create("classpath:io/cucumber/picocontainer/annotationconfig")));
        backend.buildWorld();
        verify(factory).addClass(ExamplePicoConfiguration.NestedUrlProvider.class);
    }

    @Test
    void adds_nested_provider_classes() {
        backend.loadGlue(glue,
            singletonList(URI.create("classpath:io/cucumber/picocontainer/annotationconfig")));
        backend.buildWorld();
        verify(factory).addClass(UrlToUriProvider.class);
    }

    @Test
    void finds_configured_classes_only_once_when_scanning_twice() {
        backend.loadGlue(glue, asList(
            URI.create("classpath:io/cucumber/picocontainer/annotationconfig"),
            URI.create("classpath:io/cucumber/picocontainer/annotationconfig")));
        backend.buildWorld();
        verify(factory, never()).addClass(ExamplePicoConfiguration.class);
        verify(factory, times(1)).addClass(URLConnectionProvider.class);
        verify(factory, times(1)).addClass(DatabaseConnectionProvider.class);
        verify(factory, times(1)).addClass(ExamplePicoConfiguration.NestedUrlProvider.class);
        verify(factory, times(1)).addClass(UrlToUriProvider.class);
    }

}
