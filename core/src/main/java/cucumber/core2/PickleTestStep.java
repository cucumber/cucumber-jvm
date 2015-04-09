package cucumber.core2;

import cucumber.runtime.Argument;
import cucumber.runtime.CucumberException;
import cucumber.runtime.ParameterInfo;
import cucumber.runtime.StepDefinition;
import cucumber.runtime.xstream.LocalizedXStreams;
import pickles.PickleStep;

import java.util.ArrayList;
import java.util.List;

public class PickleTestStep implements TestStep {
    private final PickleStep pickleStep;
    private final StepDefinition stepDefinition;
    private final List<Argument> arguments;
    private final LocalizedXStreams.LocalizedXStream xStream;

    public PickleTestStep(PickleStep pickleStep, StepDefinition stepDefinition, List<Argument> arguments, LocalizedXStreams.LocalizedXStream xStream) {
        this.pickleStep = pickleStep;
        this.stepDefinition = stepDefinition;
        this.arguments = arguments;
        this.xStream = xStream;
    }

    @Override
    public void run() {
        Object[] args = transformedArgs();
        try {
            stepDefinition.execute(null, args);
        } catch (Throwable throwable) {
            throw new RuntimeException(throwable);
        }
    }

    private Object[] transformedArgs() {
        int argumentCount = arguments.size();

        if (pickleStep.getArgument() != null) {
            argumentCount++;
        }
        Integer parameterCount = stepDefinition.getParameterCount();
        if (parameterCount != null && argumentCount != parameterCount) {
            throw arityMismatch(parameterCount);
        }

        List<Object> result = new ArrayList<Object>();

        int n = 0;
        for (Argument argument : arguments) {
            ParameterInfo parameterInfo = stepDefinition.getParameterType(n, String.class);
            Object arg = parameterInfo.convert(argument.getVal(), xStream);
            result.add(arg);
            n++;
        }

        // TODO: DataTables and DocStrings

//        if (step.getRows() != null) {
//            result.add(tableArgument(step, n, xStream));
//        } else if (step.getDocString() != null) {
//            result.add(step.getDocString().getValue());
//        }
        return result.toArray(new Object[result.size()]);
    }

    private CucumberException arityMismatch(int parameterCount) {
        return new CucumberException(String.format(
                "Arity mismatch: Step Definition '%s' with pattern [%s] is declared with %s parameters. However, the step has %s arguments %s. \nStep: %s",
                stepDefinition.getLocation(true),
                stepDefinition.getPattern(),
                parameterCount,
                arguments.size(),
                arguments,
                pickleStep.getText()
        ));
    }
}
