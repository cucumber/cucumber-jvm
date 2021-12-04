package io.cucumber.guice;

import io.cucumber.core.backend.Backend;
import io.cucumber.core.backend.Glue;
import io.cucumber.core.backend.ObjectFactory;
import io.cucumber.guice.integration.YourInjectorSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.net.URI;

import static java.lang.Thread.currentThread;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.samePropertyValuesAs;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;

@ExtendWith({ MockitoExtension.class })
class GuiceBackendTest {

    @Mock
    private Glue glue;

    @Mock
    private ObjectFactory factory;

    private GuiceBackend backend;

    @BeforeEach
    void createBackend() {
        this.backend = new GuiceBackend(factory, currentThread()::getContextClassLoader);
    }

    @Test
    void finds_injector_source_impls_by_classpath_url() {
        backend.loadGlue(glue, singletonList(URI.create("classpath:io/cucumber/guice/integration")));
        verify(factory).addClass(YourInjectorSource.class);
    }

    @Test
    void world_and_snippet_methods_do_nothing() {
        backend.loadGlue(glue, singletonList(URI.create("classpath:io/cucumber/guice/integration")));
        backend.buildWorld();
        backend.disposeWorld();
        assertThat(backend.getSnippet(), is(nullValue()));
    }

    @Test
    void doesnt_save_anything_in_glue() {
        backend.loadGlue(null, singletonList(URI.create("classpath:io/cucumber/guice/integration")));
        verify(factory).addClass(YourInjectorSource.class);
    }

    @Test()
    void list_of_uris_cant_be_null() {
        assertThrows(NullPointerException.class, () -> backend.loadGlue(glue, null));
    }

    @Test
    void backend_service_creates_backend() {
        GuiceBackendProviderService guiceBackendProviderService = new GuiceBackendProviderService();
        Backend backend = guiceBackendProviderService.create(factory, factory, currentThread()::getContextClassLoader);
        backend.loadGlue(glue, singletonList(URI.create("classpath:io/cucumber/guice/integration")));
        this.backend.loadGlue(glue, singletonList(URI.create("classpath:io/cucumber/guice/integration")));
        assertThat(backend, samePropertyValuesAs(this.backend));
    }

}
