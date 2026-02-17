package io.cucumber.java;

import io.cucumber.core.logging.LogRecordListener;
import io.cucumber.core.logging.LoggerFactory;
import io.cucumber.core.options.RuntimeOptions;
import io.cucumber.java.steps.Steps;
import org.junit.jupiter.api.Test;

import java.net.URI;

import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GlueLoadingAdvisorTest {
    private final RuntimeOptions options = RuntimeOptions.defaultOptions();
    private final GlueLoadingAdvisor advisor = new GlueLoadingAdvisor(options);

    @Test
    void logs_loadGlue_hints_default_options_class_without_glue() {
        LogRecordListener listener = new LogRecordListener();
        LoggerFactory.addListener(listener);
        mockLongGlueLoading(true, false, false, false);

        // When loading a lot of classes
        advisor.logGlueLoadingAdvices(singletonList(URI.create("classpath:/com")));

        // Then we log some hint message to improve the situation
        String message = listener.getLogRecords().get(0).getMessage();
        assertTrue(message.contains("Scanning the glue packages"));
        assertTrue(message.contains("remove the classes that do not contain cucumber step"));
    }

    @Test
    void logs_loadGlue_hints_default_options_public_static_inner_classes() {
        LogRecordListener listener = new LogRecordListener();
        LoggerFactory.addListener(listener);
        mockLongGlueLoading(false, true, true, false);

        // When loading a lot of classes
        advisor.logGlueLoadingAdvices(singletonList(URI.create("classpath:/com")));

        // Then we log some hint message to improve the situation
        String message = listener.getLogRecords().get(0).getMessage();
        assertTrue(message.contains("Scanning the glue packages"));
        assertTrue(message.contains("public static inner classes to private"));
    }

    @Test
    void logs_loadGlue_hints_default_options_non_public_static_inner_classes() {
        LogRecordListener listener = new LogRecordListener();
        LoggerFactory.addListener(listener);
        mockLongGlueLoading(false, true, false, true);

        // When loading a lot of classes
        advisor.logGlueLoadingAdvices(singletonList(URI.create("classpath:/com")));

        // Then we log some hint message to improve the situation
        String message = listener.getLogRecords().get(0).getMessage();
        assertTrue(message.contains("Scanning the glue packages"));
        assertTrue(message.contains("remove non-public classes from the glue package"));
    }

    @Test
    void logs_loadGlue_hints_default_options() {
        LogRecordListener listener = new LogRecordListener();
        LoggerFactory.addListener(listener);
        mockLongGlueLoading(true, true, false, false);

        // When loading a lot of classes
        advisor.logGlueLoadingAdvices(singletonList(URI.create("classpath:/com")));

        // Then we log some hint message to improve the situation
        assertTrue(listener.getLogRecords().get(0).getMessage().contains("Scanning the glue packages"));
    }

    @Test
    void logs_loadGlue_hints_default_options_glue_only_classes() {
        LogRecordListener listener = new LogRecordListener();
        LoggerFactory.addListener(listener);
        mockLongGlueLoading(false, true, false, false);

        // When loading a lot of classes
        advisor.logGlueLoadingAdvices(singletonList(URI.create("classpath:/com")));

        // Then we log some hint message to improve the situation
        assertTrue(listener.getLogRecords().isEmpty());
    }

    @Test
    void logs_loadGlue_hints_no_glue_package() {
        // When glue loading hint is disabled
        LogRecordListener listener = new LogRecordListener();
        LoggerFactory.addListener(listener);
        mockLongGlueLoading(true, true, true, false);
        options.setGlueHintEnabled(true);

        // When loading a lot of classes
        advisor.logGlueLoadingAdvices(singletonList(URI.create("classpath:/")));

        // Then no hint is displayed
        String message = listener.getLogRecords().get(0).getMessage();
        assertTrue(message.contains("Scanning the glue packages"));
        assertTrue(message.contains("By default Cucumber scans the entire classpath"));
    }

    @Test
    void logs_loadGlue_hints_no_display() {
        // When glue loading hint is disabled
        LogRecordListener listener = new LogRecordListener();
        LoggerFactory.addListener(listener);
        mockLongGlueLoading(true, true, true, false);
        options.setGlueHintEnabled(false);

        // When loading a lot of classes
        advisor.logGlueLoadingAdvices(singletonList(URI.create("classpath:/com")));

        // Then no hint is displayed
        assertTrue(listener.getLogRecords().isEmpty());
    }

    @Test
    void logs_loadGlue_hints_no_glue_no_display() {
        // When glue loading hint is disabled
        LogRecordListener listener = new LogRecordListener();
        LoggerFactory.addListener(listener);
        options.setGlueHintEnabled(true);

        // When loading a lot of classes
        advisor.logGlueLoadingAdvices(singletonList(URI.create("classpath:/com")));

        // Then no hint is displayed
        assertTrue(listener.getLogRecords().isEmpty());
    }

    @Test
    void logs_loadGlue_hints_below_threshold() {
        // Given threshold value is very high
        LogRecordListener listener = new LogRecordListener();
        LoggerFactory.addListener(listener);
        mockLongGlueLoading(true, true, true, false);
        options.setGlueHintThreshold(Integer.MAX_VALUE);

        // When loading a lot of classes
        advisor.logGlueLoadingAdvices(singletonList(URI.create("classpath:/com")));

        // Then no hint is displayed
        assertTrue(listener.getLogRecords().isEmpty());
    }

    private void mockLongGlueLoading(
            boolean addClassWithoutGlue, boolean addClassWithGlue, boolean addPublicStaticInnerClass,
            boolean addNonPublicStaticInnerClass
    ) {
        options.setGlueHintThreshold(1);

        if (addClassWithoutGlue) {
            advisor.addGlueClass(GlueLoadingAdvisor.class);
        }

        if (addClassWithGlue) {
            advisor.addGlueClass(Steps.class);
            advisor.addContainerClass(Steps.class);
        }

        if (addPublicStaticInnerClass) {
            advisor.addGlueClass(PublicStaticInnerClass.class);
        }

        if (addNonPublicStaticInnerClass) {
            advisor.addGlueClass(NonPublicStaticInnerClass.class);
        }

        try {
            // long waiting to make the threshold reached
            Thread.sleep(options.getGlueHintThreshold() * 10L);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public static class PublicStaticInnerClass {
    }

    static class NonPublicStaticInnerClass {
    }

}
