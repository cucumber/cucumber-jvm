package cucumber.runtime.jython;

import cucumber.runtime.ParameterType;
import cucumber.runtime.StepDefinition;
import cucumber.runtime.Utils;
import gherkin.formatter.Argument;
import gherkin.formatter.model.Step;
import org.python.core.PyInstance;
import org.python.core.PyList;
import org.python.core.PyObject;
import org.python.core.PyString;

import java.util.List;
import java.util.Locale;

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
        if (matched_arguments instanceof PyList) {
            return (PyList) matched_arguments;
        } else {
            return null;
        }
    }

    @Override
    public String getLocation() {
        return null;
    }

    @Override
    public List<ParameterType> getParameterTypes() {
        return Utils.listOf(arity, new ParameterType(String.class, null));
    }

    @Override
    public void execute(Locale locale, Object[] args) throws Throwable {
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
