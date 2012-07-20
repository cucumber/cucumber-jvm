package cucumber.runtime.jython;

import cucumber.runtime.HookDefinition;
import cucumber.runtime.ScenarioResult;
import gherkin.TagExpression;
import org.python.core.*;
import java.util.Collection;

public class JythonHookDefinition implements HookDefinition {
    private final PyInstance hookDefinition;
    private final TagExpression tagExpression;
    private final JythonBackend backend;

    public JythonHookDefinition(JythonBackend backend, PyInstance hookDefinition) {
        this.backend = backend;
        this.hookDefinition = hookDefinition;
        PyTuple tags = (PyTuple)hookDefinition.__dict__.__finditem__("tags");
        this.tagExpression = new TagExpression(tags);
    }

    @Override
    public void execute(ScenarioResult scenarioResult) throws Throwable {
        backend.executeHook(hookDefinition, new Object[]{scenarioResult});
    }

    @Override
    public boolean matches(Collection<String> tags) {
        return tagExpression.eval(tags);
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
