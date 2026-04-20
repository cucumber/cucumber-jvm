package io.cucumber.spring;

import io.cucumber.core.backend.Glue;
import io.cucumber.core.backend.GlueDiscoveryRequest;
import io.cucumber.core.backend.ObjectFactory;
import io.cucumber.spring.annotationconfig.AnnotationContextConfiguration;
import io.cucumber.spring.cucumbercontextconfigannotation.AbstractWithComponentAnnotation;
import io.cucumber.spring.cucumbercontextconfigannotation.AnnotatedInterface;
import io.cucumber.spring.cucumbercontextconfigannotation.WithMetaAnnotation;
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
final class SpringBackendTest {

    @Mock
    private Glue glue;

    @Mock
    private ObjectFactory factory;

    private SpringBackend backend;

    @BeforeEach
    void createBackend() {
        this.backend = new SpringBackend(factory, currentThread()::getContextClassLoader);
    }

    @Test
    void finds_annotation_context_configuration_by_classpath_url() {
        backend.loadGlue(glue,
            new TestGlueDiscoveryRequest(URI.create("classpath:io/cucumber/spring/annotationconfig")));
        backend.buildWorld();
        verify(factory).addClass(AnnotationContextConfiguration.class);
    }

    @Test
    void finds_annotation_context_configuration_by_classname() {
        backend.loadGlue(glue, new TestGlueDiscoveryRequest(AnnotationContextConfiguration.class.getName()));
        backend.buildWorld();
        verify(factory).addClass(AnnotationContextConfiguration.class);
    }

    @Test
    void finds_annotaiton_context_configuration_once_by_classpath_url() {
        backend.loadGlue(glue, new TestGlueDiscoveryRequest(
            URI.create("classpath:io/cucumber/spring/annotationconfig"),
            URI.create("classpath:io/cucumber/spring/annotationconfig")));
        backend.buildWorld();
        verify(factory, times(1)).addClass(AnnotationContextConfiguration.class);
    }

    @Test
    void ignoresAbstractClassWithCucumberContextConfiguration() {
        backend.loadGlue(glue, new TestGlueDiscoveryRequest(
            URI.create("classpath:io/cucumber/spring/cucumbercontextconfigannotation")));
        backend.buildWorld();
        verify(factory, times(0)).addClass(AbstractWithComponentAnnotation.class);
    }

    @Test
    void ignoresInterfaceWithCucumberContextConfiguration() {
        backend.loadGlue(glue, new TestGlueDiscoveryRequest(
            URI.create("classpath:io/cucumber/spring/cucumbercontextconfigannotation")));
        backend.buildWorld();
        verify(factory, times(0)).addClass(AnnotatedInterface.class);
    }

    @Test
    void considersClassWithCucumberContextConfigurationMetaAnnotation() {
        backend.loadGlue(glue, new TestGlueDiscoveryRequest(
            URI.create("classpath:io/cucumber/spring/cucumbercontextconfigannotation")));
        backend.buildWorld();
        verify(factory, times(1)).addClass(WithMetaAnnotation.class);
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
