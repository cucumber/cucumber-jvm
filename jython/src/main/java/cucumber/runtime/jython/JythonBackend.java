package cucumber.runtime.jython;

import cucumber.resources.Consumer;
import cucumber.resources.Resource;
import cucumber.resources.Resources;
import cucumber.runtime.Backend;
import cucumber.runtime.HookDefinition;
import cucumber.runtime.StepDefinition;
import gherkin.formatter.model.Step;
import org.python.core.PyInstance;
import org.python.core.PyObject;
import org.python.core.PyString;
import org.python.util.PythonInterpreter;

import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.emptyList;

public class JythonBackend implements Backend {
    private static final String DSL = "/cucumber/runtime/jython/dsl.py";
    private final PythonInterpreter jython = new PythonInterpreter();
    private final List<StepDefinition> stepDefinitions = new ArrayList<StepDefinition>();
    private PyObject world;

    public JythonBackend(List<String> scriptPaths) {
        defineStepDefinitions(scriptPaths);
    }

    private void defineStepDefinitions(List<String> scriptPaths) {
        jython.set("backend", this);
        jython.execfile(getClass().getResourceAsStream(DSL), DSL);

        for (String scriptPath : scriptPaths) {
            Resources.scan(scriptPath.replace('.', '/'), ".py", new Consumer() {
                public void consume(Resource resource) {
                    jython.execfile(resource.getInputStream(), resource.getPath());
                }
            });
        }
    }

    public void registerStepdef(PyInstance stepdef, int arity) {
        stepDefinitions.add(new JythonStepDefinition(this, stepdef, arity));
    }

    @Override
    public List<StepDefinition> getStepDefinitions() {
        return stepDefinitions;
    }

    @Override
    public void newWorld() {
        world = jython.eval("World()");
    }

    @Override
    public void disposeWorld() {
    }

    @Override
    public String getSnippet(Step step) {
        return new JythonSnippetGenerator(step).getSnippet();
    }

    @Override
    public List<HookDefinition> getBeforeHooks() {
        return emptyList();
    }

    @Override
    public List<HookDefinition> getAfterHooks() {
        return emptyList();
    }

    public void execute(PyInstance stepdef, Object[] args) {
        PyObject[] pyArgs = new PyObject[args.length + 1];
        pyArgs[0] = world;
        for (int i = 0; i < args.length; i++) {
            pyArgs[i+1] = new PyString((String) args[i]);
        }
        stepdef.invoke("execute", pyArgs);
    }
}
