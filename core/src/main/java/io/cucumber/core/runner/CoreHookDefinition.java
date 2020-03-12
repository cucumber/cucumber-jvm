package io.cucumber.core.runner;

import io.cucumber.core.backend.HookDefinition;
import io.cucumber.core.backend.ScenarioScoped;
import io.cucumber.core.backend.TestCaseState;
import io.cucumber.tagexpressions.Expression;
import io.cucumber.tagexpressions.TagExpressionParser;

import java.util.List;
import java.util.UUID;

import static java.util.Objects.requireNonNull;

class CoreHookDefinition {

    static CoreHookDefinition create(HookDefinition hookDefinition) {
        // Ideally we would avoid this by keeping the scenario scoped
        // glue in a different bucket from the globally scoped glue.
        if (hookDefinition instanceof ScenarioScoped) {
            return new ScenarioScopedCoreHookDefinition(hookDefinition);
        }
        return new CoreHookDefinition(UUID.randomUUID(), hookDefinition);
    }

    private final UUID id;
    private final HookDefinition delegate;
    private final Expression tagExpression;

    private CoreHookDefinition(UUID id, HookDefinition delegate) {
        this.id = requireNonNull(id);
        this.delegate = delegate;
        this.tagExpression = new TagExpressionParser().parse(delegate.getTagExpression());
    }

    void execute(TestCaseState scenario) {
        delegate.execute(scenario);
    }

    HookDefinition getDelegate() {
        return delegate;
    }

    String getLocation() {
        return delegate.getLocation();
    }

    UUID getId() {
        return id;
    }

    int getOrder() {
        return delegate.getOrder();
    }

    boolean matches(List<String> tags) {
        return tagExpression.evaluate(tags);
    }

    String getTagExpression() {
        return delegate.getTagExpression();
    }

    static class ScenarioScopedCoreHookDefinition extends CoreHookDefinition implements ScenarioScoped {
        private ScenarioScopedCoreHookDefinition(HookDefinition delegate) {
            super(UUID.randomUUID(), delegate);
        }

    }
}
