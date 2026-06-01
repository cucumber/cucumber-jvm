package io.cucumber.guice;

import io.cucumber.core.backend.BackendProviderService;
import io.cucumber.core.backend.Glue;
import io.cucumber.core.backend.GlueDiscoveryRequest;
import io.cucumber.core.backend.ObjectFactory;
import io.cucumber.guice.integration.YourInjectorSource;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoSettings;

import java.net.URI;
import java.util.List;
import java.util.function.Supplier;

import static java.lang.Thread.currentThread;
import static java.util.Collections.emptyList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

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
        TestGlueDiscoveryRequest request = new TestGlueDiscoveryRequest(
            URI.create("classpath:io/cucumber/guice/integration"));
        backend.loadGlue(glue, request);
        verify(factory).addClass(YourInjectorSource.class);
    }

    @Test
    void finds_injector_source_impls_once_by_classpath_url() {
        GuiceBackend backend = new GuiceBackend(factory, classLoader);
        TestGlueDiscoveryRequest request = new TestGlueDiscoveryRequest(
            URI.create("classpath:io/cucumber/guice/integration"),
            URI.create("classpath:io/cucumber/guice/integration"));
        backend.loadGlue(glue, request);
        verify(factory, times(1)).addClass(YourInjectorSource.class);
    }

    @Test
    void finds_injector_source_impls_by_classname() {
        GuiceBackend backend = new GuiceBackend(factory, classLoader);
        TestGlueDiscoveryRequest request = new TestGlueDiscoveryRequest(YourInjectorSource.class.getName());
        backend.loadGlue(glue, request);
        verify(factory).addClass(YourInjectorSource.class);
    }

    @Test
    void world_and_snippet_methods_do_nothing() {
        GuiceBackend backend = new GuiceBackend(factory, classLoader);
        TestGlueDiscoveryRequest request = new TestGlueDiscoveryRequest(
            URI.create("classpath:io/cucumber/guice/integration"));
        backend.loadGlue(glue, request);
        backend.buildWorld();
        backend.disposeWorld();
        assertThat(backend.getSnippet(), is(nullValue()));
    }

    @Test
    @SuppressWarnings("NullAway")
    void doesnt_save_anything_in_glue() {
        GuiceBackend backend = new GuiceBackend(factory, classLoader);
        TestGlueDiscoveryRequest request = new TestGlueDiscoveryRequest(
            URI.create("classpath:io/cucumber/guice/integration"));
        backend.loadGlue(null, request);
        verify(factory).addClass(YourInjectorSource.class);
    }

    @Test
    @SuppressWarnings("NullAway")
    void request_cant_be_null() {
        GuiceBackend backend = new GuiceBackend(factory, classLoader);
        assertThrows(NullPointerException.class, () -> backend.loadGlue(glue, (GlueDiscoveryRequest) null));
    }

    @Test
    void backend_service_creates_backend() {
        BackendProviderService backendProviderService = new GuiceBackendProviderService();
        assertThat(backendProviderService.create(factory, factory, classLoader), is(notNullValue()));
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
