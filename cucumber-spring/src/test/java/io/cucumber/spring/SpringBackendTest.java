package io.cucumber.spring;

import io.cucumber.core.backend.Glue;
import io.cucumber.core.backend.ObjectFactory;
import io.cucumber.spring.annotationconfig.AnnotationContextConfiguration;
import io.cucumber.spring.cucontextconfig.AbstractWithComponentAnnotation;
import io.cucumber.spring.cucontextconfig.WithMetaAnnotation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.net.URI;

import static java.lang.Thread.currentThread;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SpringBackendTest {

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
        backend.loadGlue(glue, singletonList(URI.create("classpath:io/cucumber/spring/annotationconfig")));
        backend.buildWorld();
        verify(factory).addClass(AnnotationContextConfiguration.class);
    }

    @Test
    void finds_annotaiton_context_configuration_once_by_classpath_url() {
        backend.loadGlue(glue, asList(
            URI.create("classpath:io/cucumber/spring/annotationconfig"),
            URI.create("classpath:io/cucumber/spring/annotationconfig")));
        backend.buildWorld();
        verify(factory, times(1)).addClass(AnnotationContextConfiguration.class);
    }

    @Test
    void ignoresAbstractClassWithCucumberContextConfiguration() {
        backend.loadGlue(glue, singletonList(
            URI.create("classpath:io/cucumber/spring/cucontextconfig")));
        backend.buildWorld();
        verify(factory, times(0)).addClass(AbstractWithComponentAnnotation.class);
        verify(factory, times(1)).addClass(WithMetaAnnotation.class);
    }

}
