package io.cucumber.guice;

import io.cucumber.core.backend.BackendProviderService;
import io.cucumber.core.backend.Glue;
import io.cucumber.core.backend.GlueDiscoveryRequest;
import io.cucumber.core.backend.ObjectFactory;
import io.cucumber.guice.integration.YourInjectorSource;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoSettings;

import java.util.function.Supplier;

import static io.cucumber.core.backend.GlueDiscoverySelector.selectUri;
import static java.lang.Thread.currentThread;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@MockitoSettings
class GuiceBackendTest {

    public final Supplier<ClassLoader> classLoader = currentThread()::getContextClassLoader;

    @Mock
    private Glue glue;

    @Mock
    private ObjectFactory factory;

    @Test
    void finds_injector_source_impls_by_classpath_url() {
        GuiceBackend backend = new GuiceBackend(factory, classLoader);
        var request = GlueDiscoveryRequest.builder() //
                .selectors(selectUri("classpath:io/cucumber/guice/integration")) //
                .build();
        backend.loadGlue(glue, request);
        verify(factory).addClass(YourInjectorSource.class);
    }

    @Test
    void finds_injector_source_impls_once_by_classpath_url() {
        GuiceBackend backend = new GuiceBackend(factory, classLoader);
        GlueDiscoveryRequest request = GlueDiscoveryRequest.builder() //
                .selectors(selectUri("classpath:io/cucumber/guice/integration")) //
                .selectors(selectUri("classpath:io/cucumber/guice/integration")) //
                .build();
        backend.loadGlue(glue, request);
        verify(factory, times(1)).addClass(YourInjectorSource.class);
    }

    @Test
    void world_and_snippet_methods_do_nothing() {
        GuiceBackend backend = new GuiceBackend(factory, classLoader);
        var request = GlueDiscoveryRequest.builder()
                .selectors(selectUri("classpath:io/cucumber/guice/integration")) //
                .build();
        backend.loadGlue(glue, request);
        backend.buildWorld();
        backend.disposeWorld();
        assertThat(backend.getSnippet(), is(nullValue()));
    }

    @Test
    void doesnt_save_anything_in_glue() {
        GuiceBackend backend = new GuiceBackend(factory, classLoader);
        var request = GlueDiscoveryRequest.builder() //
                .selectors(selectUri("classpath:io/cucumber/guice/integration")) //
                .build();
        backend.loadGlue(glue, request);
        verify(factory).addClass(YourInjectorSource.class);
        verifyNoInteractions(glue);
    }

    @Test
    void backend_service_creates_backend() {
        BackendProviderService backendProviderService = new GuiceBackendProviderService();
        assertThat(backendProviderService.create(factory, factory, classLoader), is(notNullValue()));
    }

}
