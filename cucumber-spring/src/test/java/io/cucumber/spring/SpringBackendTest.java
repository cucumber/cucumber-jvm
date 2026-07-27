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

import static io.cucumber.core.backend.GlueDiscoverySelector.selectUri;
import static java.lang.Thread.currentThread;
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
        var request = GlueDiscoveryRequest.builder() //
                .selectors(selectUri("classpath:io/cucumber/spring/annotationconfig")) //
                .build();
        backend.loadGlue(glue, request);
        backend.buildWorld();
        verify(factory).addClass(AnnotationContextConfiguration.class);
    }

    @Test
    void finds_annotaiton_context_configuration_once_by_classpath_url() {
        var request = GlueDiscoveryRequest.builder() //
                .selectors(selectUri("classpath:io/cucumber/spring/annotationconfig")) //
                .selectors(selectUri("classpath:io/cucumber/spring/annotationconfig")) //
                .build();
        backend.loadGlue(glue, request);
        backend.buildWorld();
        verify(factory, times(1)).addClass(AnnotationContextConfiguration.class);
    }

    @Test
    void ignoresAbstractClassWithCucumberContextConfiguration() {
        var request = GlueDiscoveryRequest.builder() //
                .selectors(selectUri("classpath:io/cucumber/spring/cucumbercontextconfigannotation")) //
                .build();
        backend.loadGlue(glue, request);
        backend.buildWorld();
        verify(factory, times(0)).addClass(AbstractWithComponentAnnotation.class);
    }

    @Test
    void ignoresInterfaceWithCucumberContextConfiguration() {
        var request = GlueDiscoveryRequest.builder() //
                .selectors(selectUri("classpath:io/cucumber/spring/cucumbercontextconfigannotation")) //
                .build();
        backend.loadGlue(glue, request);
        backend.buildWorld();
        verify(factory, times(0)).addClass(AnnotatedInterface.class);
    }

    @Test
    void considersClassWithCucumberContextConfigurationMetaAnnotation() {
        var request = GlueDiscoveryRequest.builder() //
                .selectors(selectUri("classpath:io/cucumber/spring/cucumbercontextconfigannotation")) //
                .build();
        backend.loadGlue(glue, request);
        backend.buildWorld();
        verify(factory, times(1)).addClass(WithMetaAnnotation.class);
    }

}
