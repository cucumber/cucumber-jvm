package cucumber.runtime.jython;

import cucumber.api.Scenario;
import cucumber.runtime.CucumberException;
import cucumber.runtime.HookDefinition;
import org.python.core.PyInstance;
import org.python.core.PyNone;
import org.python.core.PyObject;
import org.python.core.PyString;

public class JythonHookDefinition implements HookDefinition {
    private final PyInstance hookDefinition;
    private final String tagExpression;
    private final JythonBackend backend;

    public JythonHookDefinition(JythonBackend backend, PyInstance hookDefinition) {
        this.backend = backend;
        this.hookDefinition = hookDefinition;
        PyObject tagExpression = hookDefinition.__dict__.__finditem__("tagExpression");
        if (tagExpression instanceof PyString) {
            this.tagExpression = tagExpression.asString();
        } else if (tagExpression instanceof PyNone) {
            this.tagExpression = null;
        } else {
            throw new CucumberException("tagExpression must be a String or None");
        }
    }

    @Override
    public void execute(Scenario scenario) throws Throwable {
        backend.executeHook(hookDefinition, new Object[]{scenario});
    }

    @Override
    public String getTagExpression() {
        return tagExpression;
    }

    @Override
    public String getLocation(boolean detail) {
        return null;
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
