package io.cucumber.core.options;

import io.cucumber.core.backend.ObjectFactory;
import org.junit.Test;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class CucumberPropertiesParserTest {

    private final CucumberPropertiesParser cucumberPropertiesParser = new CucumberPropertiesParser();
    private final Map<String, String> properties = new HashMap<>();

    @Test
    public void should_parse_cucumber_options(){
        properties.put(Constants.CUCUMBER_OPTIONS_PROPERTY_NAME, "--glue com.example");
        RuntimeOptions options = cucumberPropertiesParser.parse(properties).build();
        assertThat(options.getGlue(), equalTo(singletonList(URI.create("classpath:com/example"))));
    }

    @Test
    public void should_parse_cucumber_object_factory(){
        properties.put(Constants.CUCUMBER_OBJECT_FACTORY_PROPERTY_NAME, CustomObjectFactory.class.getName());
        RuntimeOptions options = cucumberPropertiesParser.parse(properties).build();
        assertThat(options.getObjectFactoryClass(), equalTo(CustomObjectFactory.class));
    }


    private static final class CustomObjectFactory implements ObjectFactory {
        @Override
        public boolean addClass(Class<?> glueClass) {
            return false;
        }

        @Override
        public <T> T getInstance(Class<T> glueClass) {
            return null;
        }

        @Override
        public void start() {

        }

        @Override
        public void stop() {

        }
    }
}
