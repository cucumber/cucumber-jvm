package io.cucumber.junit.platform.engine;

import org.junit.platform.engine.ConfigurationParameters;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.support.discovery.DiscoveryIssueReporter;

import java.util.List;
import java.util.function.UnaryOperator;

import static io.cucumber.junit.platform.engine.DefaultDescriptorOrderingStrategy.getStrategy;

class OrderingVisitor implements TestDescriptor.Visitor {

    private final UnaryOperator<List<AbstractCucumberTestDescriptor>> orderer;

    OrderingVisitor(ConfigurationParameters configuration, DiscoveryIssueReporter issueReporter) {
        this(getStrategy(configuration).create(configuration, issueReporter));
    }

    private OrderingVisitor(UnaryOperator<List<AbstractCucumberTestDescriptor>> orderer) {
        this.orderer = orderer;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void visit(TestDescriptor descriptor) {
        descriptor.orderChildren(children -> {
            // Ok. All TestDescriptors are AbstractCucumberTestDescriptor
            @SuppressWarnings("rawtypes")
            List<AbstractCucumberTestDescriptor> cucumberDescriptors = (List) children;
            orderer.apply(cucumberDescriptors);
            return children;
        });
    }

}
