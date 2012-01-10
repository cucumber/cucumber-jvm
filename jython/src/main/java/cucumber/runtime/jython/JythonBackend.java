package cucumber.runtime.jython;

import cucumber.io.Resource;
import cucumber.io.ResourceLoader;
import cucumber.runtime.Backend;
import cucumber.runtime.CucumberException;
import cucumber.runtime.World;
import gherkin.formatter.model.Step;
import org.python.core.PyInstance;
import org.python.core.PyObject;
import org.python.core.PyString;
import org.python.util.PythonInterpreter;

import java.io.IOException;
import java.util.List;

public class JythonBackend implements Backend {
    private static final String DSL = "/cucumber/runtime/jython/dsl.py";
    private final ResourceLoader resourceLoader;
    private final PythonInterpreter jython = new PythonInterpreter();
    private PyObject pyWorld;
    private World world;

    public JythonBackend(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
        jython.set("backend", this);
        jython.execfile(getClass().getResourceAsStream(DSL), DSL);
    }

    @Override
    public void buildWorld(List<String> gluePaths, World world) {
        this.pyWorld = jython.eval("World()");
        this.world = world;

        for (String gluePath : gluePaths) {
            for (Resource resource : resourceLoader.resources(gluePath, ".py")) {
                execFile(resource);
            }
        }
    }

    private void execFile(Resource resource) {
        try {
            jython.execfile(resource.getInputStream(), resource.getPath());
        } catch (IOException e) {
            throw new CucumberException(e);
        }
    }

    @Override
    public void disposeWorld() {
    }

    @Override
    public String getSnippet(Step step) {
        return new JythonSnippetGenerator(step).getSnippet();
    }

    public void registerStepdef(PyInstance stepdef, int arity) {
        world.addStepDefinition(new JythonStepDefinition(this, stepdef, arity));
    }

    public void execute(PyInstance stepdef, Object[] args) {
        PyObject[] pyArgs = new PyObject[args.length + 1];
        pyArgs[0] = pyWorld;
        for (int i = 0; i < args.length; i++) {
            pyArgs[i + 1] = new PyString((String) args[i]);
        }
        stepdef.invoke("execute", pyArgs);
    }
}
