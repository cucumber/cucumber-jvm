package io.cucumber.junit.platform.engine;

import org.junit.platform.engine.ConfigurationParameters;

import java.util.List;
import java.util.function.UnaryOperator;

interface DescriptorOrderingStrategy {

    /**
     * Creates a unary operator used by
     * {@link org.junit.platform.engine.TestDescriptor#orderChildren(UnaryOperator)}.
     *
     * @param  configuration to pull configuration values from, never
     *                       {@code null}.
     * @return               an operator, never {@code null}.
     */
    UnaryOperator<List<CucumberTestDescriptor>> create(ConfigurationParameters configuration);

}
