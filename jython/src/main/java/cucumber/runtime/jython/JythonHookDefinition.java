package cucumber.runtime.jython;

import cucumber.api.Scenario;
import cucumber.runtime.HookDefinition;
import cucumber.runtime.TagPredicate;
import gherkin.pickles.PickleTag;
import org.python.core.PyInstance;
import org.python.core.PyTuple;

import java.util.Collection;

public class JythonHookDefinition implements HookDefinition {
    private final PyInstance hookDefinition;
    private final TagPredicate tagPredicate;
    private final JythonBackend backend;

    public JythonHookDefinition(JythonBackend backend, PyInstance hookDefinition) {
        this.backend = backend;
        this.hookDefinition = hookDefinition;
        PyTuple tags = (PyTuple) hookDefinition.__dict__.__finditem__("tags");
        this.tagPredicate = new TagPredicate(tags);
    }

    @Override
    public void execute(Scenario scenario) throws Throwable {
        backend.executeHook(hookDefinition, scenario);
    }

    @Override
    public boolean matches(Collection<PickleTag> tags) {
        return tagPredicate.apply(tags);
    }

    @Override
    public String getLocation(boolean detail) {
        return null;
    }

    @Override
    public int getOrder() {
        return 0;
    }

    @Override
    public boolean isScenarioScoped() {
        return false;
    }
}
