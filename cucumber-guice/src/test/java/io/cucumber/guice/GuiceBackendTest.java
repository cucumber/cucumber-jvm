package io.cucumber.guice;

import io.cucumber.core.backend.BackendProviderService;
import io.cucumber.core.backend.Glue;
import io.cucumber.core.backend.ObjectFactory;
import io.cucumber.guice.integration.YourInjectorSource;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.net.URI;
import java.util.function.Supplier;

import static java.lang.Thread.currentThread;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith({ MockitoExtension.class })
class GuiceBackendTest {

    public final Supplier<ClassLoader> classLoader = currentThread()::getContextClassLoader;

    @Mock
    private Glue glue;

    @Mock
    private ObjectFactory factory;

    @Test
    void finds_injector_source_impls_by_classpath_url() {
        GuiceBackend backend = new GuiceBackend(factory, classLoader);
        backend.loadGlue(glue, singletonList(URI.create("classpath:io/cucumber/guice/integration")));
        verify(factory).addClass(YourInjectorSource.class);
    }

    @Test
    void finds_injector_source_impls_once_by_classpath_url() {
        GuiceBackend backend = new GuiceBackend(factory, classLoader);
        backend.loadGlue(glue, asList(URI.create("classpath:io/cucumber/guice/integration"),
            URI.create("classpath:io/cucumber/guice/integration")));
        verify(factory, times(1)).addClass(YourInjectorSource.class);
    }

    @Test
    void world_and_snippet_methods_do_nothing() {
        GuiceBackend backend = new GuiceBackend(factory, classLoader);
        backend.loadGlue(glue, singletonList(URI.create("classpath:io/cucumber/guice/integration")));
        backend.buildWorld();
        backend.disposeWorld();
        assertThat(backend.getSnippet(), is(nullValue()));
    }

    @Test
    void doesnt_save_anything_in_glue() {
        GuiceBackend backend = new GuiceBackend(factory, classLoader);
        backend.loadGlue(null, singletonList(URI.create("classpath:io/cucumber/guice/integration")));
        verify(factory).addClass(YourInjectorSource.class);
    }

    @Test()
    void list_of_uris_cant_be_null() {
        GuiceBackend backend = new GuiceBackend(factory, classLoader);
        assertThrows(NullPointerException.class, () -> backend.loadGlue(glue, null));
    }

    @Test
    void backend_service_creates_backend() {
        BackendProviderService backendProviderService = new GuiceBackendProviderService();
        assertThat(backendProviderService.create(factory, factory, classLoader), is(notNullValue()));
    }

}
