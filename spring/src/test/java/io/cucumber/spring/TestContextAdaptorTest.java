package io.cucumber.spring;

import org.junit.jupiter.api.Test;
import org.springframework.context.support.GenericApplicationContext;

import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class TestContextAdaptorTest {

    private static final class GlueClass {

    }

    @Test
    void beanDefinitionsAreNotOverridden() {
        GenericApplicationContext context = new GenericApplicationContext();
        context.setAllowBeanDefinitionOverriding(false);

        TestContextAdaptor adaptor1 = TestContextAdaptor.createApplicationContextAdaptor(context, asList(GlueClass.class, GlueClass.class));
        adaptor1.start();
        GlueCodeContext.getInstance().start();
        GlueClass instance1 = adaptor1.getInstance(GlueClass.class);
        assertNotNull(instance1);
        adaptor1.stop();
        GlueCodeContext.getInstance().stop();
    }

}
