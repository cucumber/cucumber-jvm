package io.cucumber.junit.platform.engine;

import org.junit.platform.engine.ConfigurationParameters;
import org.junit.platform.engine.support.discovery.DiscoveryIssueReporter;

import java.util.List;
import java.util.function.UnaryOperator;

interface DescriptorOrderingStrategy {

    UnaryOperator<List<AbstractCucumberTestDescriptor>> create(
            ConfigurationParameters configuration, DiscoveryIssueReporter issueReporter
    );

}
