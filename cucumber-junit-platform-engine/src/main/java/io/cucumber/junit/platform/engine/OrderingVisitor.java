package io.cucumber.junit.platform.engine;

import org.junit.platform.engine.TestDescriptor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

import static java.util.Comparator.comparing;

class OrderingVisitor implements TestDescriptor.Visitor {

    private final UnaryOperator<List<AbstractCucumberTestDescriptor>> ordrer;

    public OrderingVisitor(UnaryOperator<List<AbstractCucumberTestDescriptor>> ordrer) {
        this.ordrer = ordrer;
    }

    @Override
    public void visit(TestDescriptor descriptor) {
        descriptor.orderChildren(children -> {
            List untypedChildren = children;
            List<AbstractCucumberTestDescriptor> cucumberDescriptors = untypedChildren;
            ordrer.apply(cucumberDescriptors);
            return children;
        });
    }

}
