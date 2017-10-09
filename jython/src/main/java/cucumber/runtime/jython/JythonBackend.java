package cucumber.runtime.jython;

import cucumber.api.DataTable;
import cucumber.api.Scenario;
import cucumber.runtime.Backend;
import cucumber.runtime.CucumberException;
import cucumber.runtime.Glue;
import cucumber.runtime.UnreportedStepExecutor;
import cucumber.runtime.io.Resource;
import cucumber.runtime.io.ResourceLoader;
import cucumber.runtime.snippets.FunctionNameGenerator;
import cucumber.runtime.snippets.SnippetGenerator;
import gherkin.pickles.PickleStep;
import org.python.core.Py;
import org.python.core.PyException;
import org.python.core.PyInstance;
import org.python.core.PyList;
import org.python.core.PyObject;
import org.python.core.PyString;
import org.python.util.PythonInterpreter;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;

public class JythonBackend implements Backend {
    private final SnippetGenerator snippetGenerator = new SnippetGenerator(new JythonSnippet());
    private final ResourceLoader resourceLoader;
    private final PythonInterpreter jython;
    private PyObject pyWorld;
    private Glue glue;

    public JythonBackend(ResourceLoader resourceLoader, PythonInterpreter jython) {
        this.resourceLoader = resourceLoader;
        this.jython = jython;
        jython.set("backend", this);

        for (Resource resource : resourceLoader.resources("classpath:cucumber/runtime/jython", "dsl.py")) {
            runScript(resource);
        }
    }

    public JythonBackend(ResourceLoader resourceLoader) {
        this(resourceLoader, new PythonInterpreter());
    }

    @Override
    public void loadGlue(Glue glue, List<String> gluePaths) {
        this.glue = glue;

        for (String gluePath : gluePaths) {
            for (Resource resource : resourceLoader.resources(gluePath, ".py")) {
                runScript(resource);
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

    private void runScript(Resource resource) {
        try {
            jython.execfile(resource.getInputStream(), resource.getAbsolutePath());
        } catch (IOException e) {
            throw new CucumberException(e);
        }
    }

    @Override
    public void disposeWorld() {
    }

    @Override
    public String getSnippet(PickleStep step, String keyword, FunctionNameGenerator functionNameGenerator) {
        return snippetGenerator.getSnippet(step, keyword, functionNameGenerator);
    }

    public void registerStepdef(PyInstance stepdef, int arity) {
        glue.addStepDefinition(new JythonStepDefinition(this, stepdef, arity));
    }

    public void addBeforeHook(PyInstance hookDefinition) {
        glue.addBeforeHook(new JythonHookDefinition(this, hookDefinition));
    }

    public void addAfterHook(PyInstance hookDefinition) {
        glue.addAfterHook(new JythonHookDefinition(this, hookDefinition));
    }

    public void executeHook(PyInstance hookDefinition, Scenario scenario) {
        try {
            // Try to pass the scenario
            hookDefinition.invoke("execute", pyWorld, Py.java2py(scenario));
        } catch (PyException e) {
            if (getStacktrace(e).contains("takes exactly 1 argument (2 given)")) {
                // The stepdef doesn't want the scenario
                hookDefinition.invoke("execute", pyWorld);
            } else {
                // Some other error. Just rethrow.
                throw e;
            }
        }
    }

    private String getStacktrace(PyException e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        return sw.getBuffer().toString();
    }

    private PyObject argToPyObject(Object arg) {
        if (arg instanceof DataTable) {
            return dataTableToPyArray((DataTable) arg);
        }
        return new PyString((String) arg);
    }

    private PyObject dataTableToPyArray(DataTable table) {
        PyList pyTable = new PyList();
        for (List<String> row : table.raw()) {
            PyList pyRow = new PyList();
            for (String cell : row) {
                pyRow.append(new PyString(cell));
            }
            pyTable.append(pyRow);
        }
        return pyTable;
    }

    public void execute(PyInstance stepdef, Object[] args) throws Throwable {

        PyObject[] pyArgs = new PyObject[args.length + 1];
        pyArgs[0] = pyWorld;
        for (int i = 0; i < args.length; i++) {
            pyArgs[i + 1] = argToPyObject(args[i]);
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
