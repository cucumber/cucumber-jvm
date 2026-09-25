package io.cucumber.compatibility;

import io.cucumber.core.cli.CommandlineOptions;
import io.cucumber.core.cli.Main;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;

import java.nio.file.Path;
import java.util.List;

import static io.cucumber.junit.platform.engine.Constants.EXECUTION_ORDER_PROPERTY_NAME;
import static io.cucumber.junit.platform.engine.Constants.GLUE_PROPERTY_NAME;
import static io.cucumber.junit.platform.engine.Constants.PLUGIN_PROPERTY_NAME;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectPackage;
import static org.junit.platform.launcher.EngineFilter.includeEngines;

interface NdjsonReportWriter {
    static List<NdjsonReportWriter> list() {
        return List.of(new RuntimeNdjsonReportWriter(), new JunitPlatformNdjsonReportWriter());
    }

    String name();

    void writeTo(TestCase testCase, Path target);

    final class RuntimeNdjsonReportWriter implements NdjsonReportWriter {
        @Override
        public String name() {
            return "cucumber-cli";
        }

        @Override
        public void writeTo(TestCase testCase, Path target) {
            try {
                var order = "multiple-features-reversed".equals(testCase.getId()) ? "reverse" : "lexical";
                Main.run(
                    testCase.getFeatureWithLines().toString(), //
                    CommandlineOptions.GLUE, testCase.getGluePackageName(), //
                    CommandlineOptions.ORDER, order, //
                    CommandlineOptions.PLUGIN, "pretty", //
                    CommandlineOptions.PLUGIN, "message:" + target //
                );
            } catch (Exception e) {
                // exception: Scenario with unknown parameter types fails by
                // throwing an exceptions
                if (!"unknown-parameter-type".equals(testCase.getId())) {
                    throw e;
                }
            }
        }

        @Override
        public String toString() {
            return name();
        }
    }

    final class JunitPlatformNdjsonReportWriter implements NdjsonReportWriter {

        @Override
        public String name() {
            return "cucunmber-junit-platform-engine";
        }

        @Override
        public void writeTo(TestCase testCase, Path target) {
            var order = "multiple-features-reversed".equals(testCase.getId()) ? "reverse" : "lexical";
            try (var session = LauncherFactory.openSession()) {
                session.getLauncher().execute(LauncherDiscoveryRequestBuilder.request() //
                        .filters(includeEngines("cucumber")) //
                        .configurationParameter(GLUE_PROPERTY_NAME, testCase.getGluePackageName()) //
                        .configurationParameter(PLUGIN_PROPERTY_NAME, "pretty,summary,message:" + target) //
                        .configurationParameter(EXECUTION_ORDER_PROPERTY_NAME, order) //
                        .selectors(selectPackage(testCase.getFeaturePackageName())) //
                        .build());
            }
        }

        @Override
        public String toString() {
            return name();
        }
    }
}
