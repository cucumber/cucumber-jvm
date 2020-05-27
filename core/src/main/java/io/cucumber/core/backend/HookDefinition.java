package io.cucumber.core.backend;

import io.cucumber.tagexpressions.Expression;
import org.apiguardian.api.API;

@API(status = API.Status.STABLE)
public interface HookDefinition extends Located {

    void execute(TestCaseState state);

    Expression getTagExpression();

    int getOrder();

}
