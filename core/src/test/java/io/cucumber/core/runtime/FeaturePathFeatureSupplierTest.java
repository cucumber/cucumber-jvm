package io.cucumber.core.runtime;

import io.cucumber.core.feature.FeatureParser;
import io.cucumber.core.feature.FeaturePath;
import io.cucumber.core.feature.Options;
import io.cucumber.core.logging.LogRecordListener;
import io.cucumber.core.logging.LoggerFactory;
import io.cucumber.core.logging.WithLogRecordListener;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.UUID;
import java.util.function.Supplier;

import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@WithLogRecordListener
class FeaturePathFeatureSupplierTest {

    private final Supplier<ClassLoader> classLoader = FeaturePathFeatureSupplierTest.class::getClassLoader;
    private final FeatureParser parser = new FeatureParser(UUID::randomUUID);

    @Test
    void logs_message_if_no_features_are_found(LogRecordListener logRecordListener) {
        Options featureOptions = () -> singletonList(FeaturePath.parse("classpath:io/cucumber/core/options"));

        FeaturePathFeatureSupplier supplier = new FeaturePathFeatureSupplier(classLoader, featureOptions, parser);
        supplier.get();
        assertThat(logRecordListener.getLogRecords().get(1).getMessage(),
            equalTo("No features found at classpath:io/cucumber/core/options"));
    }

    @Test
    void logs_message_if_no_feature_paths_are_given(LogRecordListener logRecordListener) {
        Options featureOptions = Collections::emptyList;

        FeaturePathFeatureSupplier supplier = new FeaturePathFeatureSupplier(classLoader, featureOptions, parser);
        supplier.get();
        assertThat(logRecordListener.getLogRecords().get(1).getMessage(),
            containsString("Got no path to feature directory or feature file"));
    }

    @Test
    void throws_if_path_does_not_exist() {
        Options featureOptions = () -> singletonList(FeaturePath.parse("file:does/not/exist"));
        FeaturePathFeatureSupplier supplier = new FeaturePathFeatureSupplier(classLoader, featureOptions, parser);
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            supplier::get);
        assertThat(exception.getMessage(), startsWith("path must exist: "));
    }

    @Test
    void throws_if_feature_is_empty() {
        Options featureOptions = () -> singletonList(
            FeaturePath.parse("classpath:io/cucumber/core/runtime/empty.feature"));
        FeaturePathFeatureSupplier supplier = new FeaturePathFeatureSupplier(classLoader, featureOptions, parser);
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            supplier::get);

        assertThat(exception.getMessage(), is("Feature not found: classpath:io/cucumber/core/runtime/empty.feature"));
    }

    @Test
    void throws_if_feature_does_not_exist() {
        Options featureOptions = () -> singletonList(FeaturePath.parse("classpath:no-such.feature"));
        FeaturePathFeatureSupplier supplier = new FeaturePathFeatureSupplier(classLoader, featureOptions, parser);
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            supplier::get);

        assertThat(exception.getMessage(), is("Feature not found: classpath:no-such.feature"));
    }

}
