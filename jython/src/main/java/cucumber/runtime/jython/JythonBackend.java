package cucumber.runtime.jython;

import cucumber.io.Resource;
import cucumber.io.ResourceLoader;
import cucumber.runtime.Backend;
import cucumber.runtime.CucumberException;
import cucumber.runtime.Glue;
import cucumber.runtime.UnreportedStepExecutor;
import cucumber.runtime.snippets.SnippetGenerator;
import gherkin.formatter.model.Step;
import org.python.core.PyException;
import org.python.core.PyInstance;
import org.python.core.PyObject;
import org.python.core.PyString;
import org.python.util.PythonInterpreter;

import java.io.IOException;
import java.util.List;

public class JythonBackend implements Backend {
    private static final String DSL = "/cucumber/runtime/jython/dsl.py";
    private final SnippetGenerator snippetGenerator = new SnippetGenerator(new JythonSnippet());
    private final ResourceLoader resourceLoader;
    private final PythonInterpreter jython;
    private PyObject pyWorld;
    private Glue glue;

    public JythonBackend(ResourceLoader resourceLoader, PythonInterpreter jython) {
        this.resourceLoader = resourceLoader;
        this.jython = jython;
        jython.set("backend", this);
        jython.execfile(getClass().getResourceAsStream(DSL), DSL);
    }

    public JythonBackend(ResourceLoader resourceLoader) {
        this(resourceLoader, new PythonInterpreter());
    }

    @Override
    public void loadGlue(Glue glue, List<String> gluePaths) {
        this.glue = glue;

        for (String gluePath : gluePaths) {
            for (Resource resource : resourceLoader.resources(gluePath, ".py")) {
                execFile(resource);
            }
        }
    }

    @Override
    public void setUnreportedStepExecutor(UnreportedStepExecutor executor) {
        //Not used yet
    }

    @Override
    public void buildWorld() {
        this.pyWorld = jython.eval("World()");
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
        return snippetGenerator.getSnippet(step);
    }

    public void registerStepdef(PyInstance stepdef, int arity) {
        glue.addStepDefinition(new JythonStepDefinition(this, stepdef, arity));
    }

    public void execute(PyInstance stepdef, Object[] args) throws Throwable {
        PyObject[] pyArgs = new PyObject[args.length + 1];
        pyArgs[0] = pyWorld;
        for (int i = 0; i < args.length; i++) {
            pyArgs[i + 1] = new PyString((String) args[i]);
        }
        try {
            stepdef.invoke("execute", pyArgs);
        } catch (PyException t) {
            Object unwrapped = t.value.__tojava__(Object.class);
            if (unwrapped instanceof Throwable) {
                throw (Throwable) unwrapped;
            } else {
                throw t.getCause() == null ? t : t.getCause();
            }
        }
    }
}
