package io.cucumber.spring;

import io.cucumber.core.backend.ObjectFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;

import java.util.concurrent.atomic.AtomicInteger;

import static io.cucumber.spring.CucumberTestContext.SCOPE_CUCUMBER_GLUE;
import static org.junit.Assert.assertNotEquals;

class Issue1970 {

    @Test
    public void issue1970() {
        ObjectFactory factory = new SpringFactory();
        factory.addClass(GlueClass.class); // Add glue with Spring configuration
        factory.start();
        GlueClass instance = factory.getInstance(GlueClass.class);
        String response = instance.service.get();
        factory.stop();
        factory.start();
        GlueClass instance2 = factory.getInstance(GlueClass.class);
        String response2 = instance2.service.get();
        factory.stop();

        assertNotEquals(response, response2);
    }

    @CucumberContextConfiguration
    @ContextConfiguration(classes = TestApplicationConfiguration.class)
    public static class GlueClass {

        @Autowired
        ExampleService service;

    }

    @Configuration
    public static class TestApplicationConfiguration {

        @Bean
        public BeanFactoryPostProcessor beanFactoryPostProcessor() {
            return factory -> factory.registerScope(SCOPE_CUCUMBER_GLUE, new CucumberScenarioScope());
        }

        @Bean
        public ExampleService service(ScenarioScopedApi api) {
            return new ExampleService(api);
        }

        @Bean
        @ScenarioScope
        public ScenarioScopedApi api() {
            return new ScenarioScopedApi();
        }

    }

    public static class ExampleService {

        final ScenarioScopedApi api;

        public ExampleService(ScenarioScopedApi api) {
            this.api = api;
        }

        String get() {
            return "Api response: " + api.get();
        }
    }

    public static class ScenarioScopedApi {

        private static final AtomicInteger globalCounter = new AtomicInteger(0);
        private final int instanceId = globalCounter.getAndIncrement();

        public String get() {
            return "instance " + instanceId;
        }

    }

}
