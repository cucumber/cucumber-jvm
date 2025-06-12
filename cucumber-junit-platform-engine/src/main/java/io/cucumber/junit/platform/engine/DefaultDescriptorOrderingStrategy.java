package io.cucumber.junit.platform.engine;

import io.cucumber.core.logging.Logger;
import io.cucumber.core.logging.LoggerFactory;
import org.junit.platform.engine.ConfigurationParameters;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.function.UnaryOperator;

import static io.cucumber.junit.platform.engine.Constants.EXECUTION_ORDER_PROPERTY_NAME;
import static io.cucumber.junit.platform.engine.Constants.EXECUTION_ORDER_RANDOM_SEED_PROPERTY_NAME;

enum DefaultDescriptorOrderingStrategy implements DescriptorOrderingStrategy {

    LEXICAL {
        @Override
        public UnaryOperator<List<CucumberTestDescriptor>> create(ConfigurationParameters configuration) {
            return pickles -> {
                pickles.sort(lexical);
                return pickles;
            };
        }
    },
    REVERSE {
        @Override
        public UnaryOperator<List<CucumberTestDescriptor>> create(ConfigurationParameters configuration) {
            return pickles -> {
                pickles.sort(lexical.reversed());
                return pickles;
            };
        }
    },
    RANDOM {
        @Override
        public UnaryOperator<List<CucumberTestDescriptor>> create(ConfigurationParameters configuration) {
            long seed = configuration
                    .get(EXECUTION_ORDER_RANDOM_SEED_PROPERTY_NAME, Long::decode)
                    .orElseGet(this::createRandomSeed);
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

        private long createRandomSeed() {
            long generatedSeed = Math.abs(new Random().nextLong());
            log.config(() -> String.format("Using generated seed for configuration parameter '%s' with value '%s'.",
                EXECUTION_ORDER_RANDOM_SEED_PROPERTY_NAME, generatedSeed));
            return generatedSeed;
        }
    };
    private static final Logger log = LoggerFactory.getLogger(DefaultDescriptorOrderingStrategy.class);

    private static final Comparator<CucumberTestDescriptor> lexical = Comparator
            .comparing(CucumberTestDescriptor::getUri)
            .thenComparing(CucumberTestDescriptor::getLocation);

    static DefaultDescriptorOrderingStrategy getStrategy(ConfigurationParameters configurationParameters) {
        return valueOf(
            configurationParameters.get(EXECUTION_ORDER_PROPERTY_NAME).orElse("lexical").toUpperCase(Locale.ROOT));
    }
}
