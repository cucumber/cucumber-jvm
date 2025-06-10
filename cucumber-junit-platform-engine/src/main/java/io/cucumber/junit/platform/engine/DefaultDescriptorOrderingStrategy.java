package io.cucumber.junit.platform.engine;

import org.junit.platform.engine.ConfigurationParameters;
import org.junit.platform.engine.DiscoveryIssue;
import org.junit.platform.engine.support.discovery.DiscoveryIssueReporter;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.function.UnaryOperator;

import static io.cucumber.junit.platform.engine.Constants.EXECUTION_ORDER_PROPERTY_NAME;
import static io.cucumber.junit.platform.engine.Constants.EXECUTION_ORDER_RANDOM_SEED_PROPERTY_NAME;
import static org.junit.platform.engine.DiscoveryIssue.Severity.INFO;

enum DefaultDescriptorOrderingStrategy implements DescriptorOrderingStrategy {

    LEXICAL {
        @Override
        public UnaryOperator<List<CucumberTestDescriptor>> create(
                ConfigurationParameters configuration, DiscoveryIssueReporter issueReporter
        ) {
            return pickles -> {
                pickles.sort(lexical);
                return pickles;
            };
        }
    },
    REVERSE {
        @Override
        public UnaryOperator<List<CucumberTestDescriptor>> create(
                ConfigurationParameters configuration, DiscoveryIssueReporter issueReporter
        ) {
            return pickles -> {
                pickles.sort(lexical.reversed());
                return pickles;
            };
        }
    },
    RANDOM {
        @Override
        public UnaryOperator<List<CucumberTestDescriptor>> create(
                ConfigurationParameters configuration, DiscoveryIssueReporter issueReporter
        ) {
            long seed = configuration
                    .get(EXECUTION_ORDER_RANDOM_SEED_PROPERTY_NAME, Long::decode)
                    .orElseGet(() -> createRandomSeed(issueReporter));
            // Invoked multiple times, keep state outside of closure.
            Random random = new Random(seed);
            return testDescriptors -> {
                // Sort in expected order first to remove arbitrary initial
                // ordering before applying a deterministic shuffle.
                testDescriptors.sort(lexical);
                Collections.shuffle(testDescriptors, random);
                return testDescriptors;
            };

        }

        private long createRandomSeed(DiscoveryIssueReporter issueReporter) {
            long generatedSeed = Math.abs(new Random().nextLong());
            String message = String.format("Property %s was not set. Using random value: %d",
                EXECUTION_ORDER_RANDOM_SEED_PROPERTY_NAME, generatedSeed);
            issueReporter.reportIssue(DiscoveryIssue.create(INFO, message));
            return generatedSeed;
        }

    },
    CUSTOM {
        @Override
        public UnaryOperator<List<CucumberTestDescriptor>> create(
                ConfigurationParameters configuration, DiscoveryIssueReporter issueReporter
        ) {
            return null;
        }
    };

    private static final Comparator<CucumberTestDescriptor> lexical = Comparator
            .comparing(CucumberTestDescriptor::getUri)
            .thenComparing(CucumberTestDescriptor::getLocation);

    static DefaultDescriptorOrderingStrategy getStrategy(ConfigurationParameters configurationParameters) {
        return valueOf(
            configurationParameters.get(EXECUTION_ORDER_PROPERTY_NAME).orElse("lexical").toUpperCase(Locale.ROOT));
    }
}
