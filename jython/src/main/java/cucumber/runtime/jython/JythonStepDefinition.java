package cucumber.runtime.jython;

import cucumber.runtime.ParameterInfo;
import cucumber.runtime.StepDefinition;
import cucumber.runtime.Argument;
import gherkin.pickles.PickleStep;
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
    public List<Argument> matchedArguments(PickleStep step) {
        PyObject stepText = new PyString(step.getText());
        PyObject matched_arguments = stepdef.invoke("matched_arguments", stepText);
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
    public ParameterInfo getParameterType(int n, Type argumentType) {
        return new ParameterInfo(argumentType, null, null, null);
    }

    @Override
    public void execute(String language, Object[] args) throws Throwable {
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

    @Override
    public boolean isScenarioScoped() {
        return false;
    }
}
