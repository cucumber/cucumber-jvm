package io.cucumber.junit.platform.engine;

import org.junit.platform.engine.ConfigurationParameters;

import java.util.List;
import java.util.function.UnaryOperator;

interface DescriptorOrderingStrategy {

    UnaryOperator<List<CucumberTestDescriptor>> create(
            ConfigurationParameters configuration
    );

}
