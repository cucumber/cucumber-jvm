package cucumber.runtime.jython;

import cucumber.runtime.StepDefinition;
import cucumber.runtime.Utils;
import gherkin.formatter.Argument;
import gherkin.formatter.model.Step;
import org.python.core.*;

import java.util.List;

public class JythonStepDefinition implements StepDefinition {
    private final JythonBackend jythonBackend;
    private final PyInstance stepdef;
    private final int arity;

    public JythonStepDefinition(JythonBackend jythonBackend, PyInstance stepdef, int arity) {
        this.jythonBackend = jythonBackend;
        this.stepdef = stepdef;
        this.arity = arity;
    }

    @Override
    public List<Argument> matchedArguments(Step step) {
        PyObject stepName = new PyString(step.getName());
        PyObject matched_arguments = stepdef.invoke("matched_arguments", stepName);
        if(matched_arguments instanceof PyList) {
            return (PyList) matched_arguments;
        } else {
            return null;
        }
    }

    @Override
    public Class getTypeForTableList(int argIndex) {
        return null;
    }

    @Override
    public String getLocation() {
        return null;
    }

    @Override
    public Class<?>[] getParameterTypes() {
        return Utils.classArray(arity, String.class);
    }

    @Override
    public void execute(Object[] args) throws Throwable {
        jythonBackend.execute(stepdef, args);
    }

    @Override
    public boolean isDefinedAt(StackTraceElement stackTraceElement) {
        return false;
    }

    @Override
    public String getPattern() {
        return stepdef.invoke("pattern").toString();
    }
}
