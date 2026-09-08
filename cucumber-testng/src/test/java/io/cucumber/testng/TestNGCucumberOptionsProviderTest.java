package io.cucumber.testng;

import io.cucumber.core.backend.ObjectFactory;
import io.cucumber.core.eventbus.IncrementingUuidGenerator;
import io.cucumber.core.options.CucumberOptionsAnnotationParser;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

final class TestNGCucumberOptionsProviderTest {

    private final TestNGCucumberOptionsProvider optionsProvider = new TestNGCucumberOptionsProvider();

    @Test
    void testObjectFactoryWhenNotSpecified() {
        var options = this.optionsProvider
                .getOptions(ClassWithDefault.class);

        assertThat(options)
                .extracting(CucumberOptionsAnnotationParser.CucumberOptions::objectFactory)
                .isNull();
    }

    @Test
    void testObjectFactory() {
        var options = this.optionsProvider
                .getOptions(ClassWithCustomObjectFactory.class);

        assertThat(options)
                .extracting(CucumberOptionsAnnotationParser.CucumberOptions::objectFactory)
                .isEqualTo(TestObjectFactory.class);
    }

    @Test
    void testUuidGeneratorWhenNotSpecified() {
        var options = this.optionsProvider
                .getOptions(ClassWithDefault.class);

        assertThat(options)
                .extracting(CucumberOptionsAnnotationParser.CucumberOptions::uuidGenerator)
                .isNull();
    }

    @Test
    void testUuidGenerator() {
        var options = this.optionsProvider
                .getOptions(ClassWithCustomUuidGenerator.class);

        assertThat(options)
                .extracting(CucumberOptionsAnnotationParser.CucumberOptions::uuidGenerator)
                .isEqualTo(IncrementingUuidGenerator.class);
    }

    @Test
    void includedGlueClassNamePatterns() {
        var options = this.optionsProvider
                .getOptions(ClassWithIncludedGlueClassNamePatterns.class);

        assertThat(options)
                .extracting(CucumberOptionsAnnotationParser.CucumberOptions::includedGlueClassNamePatterns)
                .isEqualTo(new String[] { ".*NounStepDefinitions?" });
    }

    @Test
    void excludedGlueClassNamePatterns() {
        CucumberOptionsAnnotationParser.CucumberOptions options = this.optionsProvider
                .getOptions(ClassWithExcludedGlueClassNamePatterns.class);

        assertThat(options)
                .extracting(CucumberOptionsAnnotationParser.CucumberOptions::excludedGlueClassNamePatterns)
                .isEqualTo(new String[] { ".*UnwantedStepDefinitions?" });

    }

    @CucumberOptions
    private static final class ClassWithDefault {

    }

    @CucumberOptions(objectFactory = TestObjectFactory.class)
    private static final class ClassWithCustomObjectFactory {

    }

    @CucumberOptions(uuidGenerator = IncrementingUuidGenerator.class)
    private static final class ClassWithCustomUuidGenerator {

    }

    @CucumberOptions(includedGlueClassNamePatterns = ".*NounStepDefinitions?")
    private static final class ClassWithIncludedGlueClassNamePatterns {

    }

    @CucumberOptions(excludedGlueClassNamePatterns = ".*UnwantedStepDefinitions?")
    private static final class ClassWithExcludedGlueClassNamePatterns {

    }

    private static final class TestObjectFactory implements ObjectFactory {

        @Override
        public boolean addClass(Class<?> glueClass) {
            return false;
        }

        @Override
        public <T> T getInstance(Class<T> glueClass) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void start() {
        }

        @Override
        public void stop() {
        }

    }

}
