package io.cucumber.spring;

import org.junit.jupiter.api.Test;
import org.springframework.context.support.GenericApplicationContext;

import static io.cucumber.spring.TestContextAdaptor.createApplicationContextAdaptor;
import static io.cucumber.spring.TestContextAdaptor.createClassPathXmlApplicationContextAdaptor;
import static io.cucumber.spring.TestContextAdaptor.createGenericApplicationContextAdaptor;
import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TestContextAdaptorTest {

    private static final class GlueClass {

    }

    @Test
    void beanDefinitionsAreNotOverridden() {
        GenericApplicationContext context = new GenericApplicationContext();
        context.setAllowBeanDefinitionOverriding(false);

        TestContextAdaptor adaptor1 = createApplicationContextAdaptor(context, asList(GlueClass.class, GlueClass.class));
        adaptor1.start();
        GlueClass instance1 = adaptor1.getInstance(GlueClass.class);
        assertNotNull(instance1);
        adaptor1.stop();
    }

    @Test
    void xmlApplicationContextCanBeReUsedBetweenScenarios() {
        String[] configLocations = {"cucumber.xml"};
        TestContextAdaptor adaptor1 = createClassPathXmlApplicationContextAdaptor(configLocations, asList(GlueClass.class, GlueClass.class));
        adaptor1.start();
        GlueClass instance1 = adaptor1.getInstance(GlueClass.class);
        assertNotNull(instance1);
        adaptor1.stop();

        adaptor1.start();
        GlueClass instance2 = adaptor1.getInstance(GlueClass.class);
        assertNotNull(instance2);
        assertNotSame(instance1, instance2);
        adaptor1.stop();
    }

    @Test
    void configurableApplicationContextCanNotBeReused() {
        TestContextAdaptor adaptor = createGenericApplicationContextAdaptor(asList(GlueClass.class, GlueClass.class));
        adaptor.start();
        adaptor.stop();

        IllegalStateException exception = assertThrows(IllegalStateException.class, adaptor::start);
        assertThat(exception.getMessage(), containsString("GenericApplicationContext does not support multiple refresh attempts"));
    }

}
