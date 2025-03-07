package io.cucumber.junit.platform.engine;

import org.junit.platform.engine.TestDescriptor;

import java.util.List;
import java.util.function.UnaryOperator;

class OrderingVisitor implements TestDescriptor.Visitor {

    private final UnaryOperator<List<AbstractCucumberTestDescriptor>> ordrer;

    public OrderingVisitor(UnaryOperator<List<AbstractCucumberTestDescriptor>> ordrer) {
        this.ordrer = ordrer;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void visit(TestDescriptor descriptor) {
        descriptor.orderChildren(children -> {
            // Ok. All TestDescriptors are AbstractCucumberTestDescriptor
            @SuppressWarnings("rawtypes")
            List<AbstractCucumberTestDescriptor> cucumberDescriptors = (List) children;
            ordrer.apply(cucumberDescriptors);
            return children;
        });
    }

}
