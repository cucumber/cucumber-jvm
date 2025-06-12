package io.cucumber.junit.platform.engine;

import org.junit.platform.engine.ConfigurationParameters;
import org.junit.platform.engine.TestDescriptor;

import java.util.List;
import java.util.function.UnaryOperator;

import static io.cucumber.junit.platform.engine.DefaultDescriptorOrderingStrategy.getStrategy;

class OrderingVisitor implements TestDescriptor.Visitor {

    private final UnaryOperator<List<CucumberTestDescriptor>> orderer;

    OrderingVisitor(ConfigurationParameters configuration) {
        this(getStrategy(configuration).create(configuration));
    }

    private OrderingVisitor(UnaryOperator<List<CucumberTestDescriptor>> orderer) {
        this.orderer = orderer;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void visit(TestDescriptor descriptor) {
        descriptor.orderChildren(children -> {
            // Ok. All TestDescriptors are AbstractCucumberTestDescriptor
            @SuppressWarnings("rawtypes")
            List<CucumberTestDescriptor> cucumberDescriptors = (List) children;
            orderer.apply(cucumberDescriptors);
            return children;
        });
    }

}
