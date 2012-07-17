package cucumber.runtime.jython;

import cucumber.runtime.ParameterType;
import cucumber.runtime.StepDefinition;
import gherkin.I18n;
import gherkin.formatter.Argument;
import gherkin.formatter.model.Step;
import org.python.core.PyInstance;
import org.python.core.PyList;
import org.python.core.PyObject;
import org.python.core.PyString;

import java.lang.reflect.Type;
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
        if (matched_arguments instanceof PyList) {
            return (PyList) matched_arguments;
        } else {
            return null;
        }
    }

    @Override
    public String getLocation(boolean detail) {
        return null;
    }

    @Override
    public Integer getParameterCount() {
        return arity;
    }

    @Override
    public ParameterType getParameterType(int n, Type argumentType) {
        return new ParameterType(argumentType, null, null);
    }

    @Override
    public void execute(I18n i18n, Object[] args) throws Throwable {
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
