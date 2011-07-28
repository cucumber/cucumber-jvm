package cucumber.runtime.ioke;

import gherkin.formatter.model.Step;
import ioke.lang.IokeObject;
import ioke.lang.Message;
import ioke.lang.Runtime;
import ioke.lang.exceptions.ControlFlow;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import cucumber.Table;
import cucumber.classpath.Classpath;
import cucumber.classpath.Consumer;
import cucumber.classpath.Input;
import cucumber.runtime.Backend;
import cucumber.runtime.CucumberException;
import cucumber.runtime.StepDefinition;

public class IokeBackend implements Backend {
    private final Runtime ioke;
    private final List<Runtime.RescueInfo> failureRescues;
    private final List<Runtime.RescueInfo> pendingRescues;
    private final List<StepDefinition> stepDefinitions = new ArrayList<StepDefinition>();
    private String currentLocation;

    public IokeBackend(String scriptPath) {
        try {
            ioke = new Runtime();
            ioke.init();
            ioke.ground.setCell("IokeBackend", this);
            ioke.evaluateString("use(\"cucumber/runtime/ioke/dsl\")");

            failureRescues = createRescues("ISpec", "ExpectationNotMet");
            pendingRescues = createRescues("Pending");

            Classpath.scan(scriptPath, ".ik", new Consumer() {
                public void consume(Input input) {
                    try {
                        currentLocation = input.getPath();
                        ioke.evaluateString("use(\"" + input.getPath() + "\")");
                    } catch (ControlFlow controlFlow) {
                        throw new CucumberException("Failed to load " + input.getPath(), controlFlow);
                    }
                }
            });

        } catch (Throwable e) {
            throw new CucumberException("Failed to initialize Ioke", e);
        }
    }

    public void addStepDefinition(Object iokeStepDefObject, Locale locale) throws Throwable {
        stepDefinitions.add(new IokeStepDefinition(this, ioke, (IokeObject) iokeStepDefObject, currentLocation, locale));
    }

    public List<StepDefinition> getStepDefinitions() {
        return stepDefinitions;
    }

    public void newWorld() {
    }

    public void disposeWorld() {
    }

    public String getSnippet(Step step) {
        return new IokeSnippetGenerator(step).getSnippet();
    }

    private List<Runtime.RescueInfo> createRescues(String... names) throws ControlFlow {
        IokeObject condition = IokeObject.as(IokeObject.getCellChain(ioke.condition,
                ioke.message,
                ioke.ground,
                names), ioke.ground);
        List<Runtime.RescueInfo> rescues = new ArrayList<Runtime.RescueInfo>();
        IokeObject rr = IokeObject.as(((Message) IokeObject.data(ioke.mimic)).sendTo(ioke.mimic, ioke.ground, ioke.rescue), ioke.ground);
        List<Object> conds = new ArrayList<Object>();
        conds.add(condition);
        rescues.add(new Runtime.RescueInfo(rr, conds, rescues, ioke.getBindIndex()));
        return rescues;
    }

    void execute(IokeObject iokeStepDefObject, Object[] args) throws Throwable {
        try {
            ioke.registerRescues(failureRescues);
            ioke.registerRescues(pendingRescues);
            invoke(iokeStepDefObject, "invoke", multilineArg(args));
        } catch (ControlFlow.Rescue e) {
            // We may handle these differently in the future...
            if (e.getRescue().token == pendingRescues) {
                throw e;
            } else if (e.getRescue().token == failureRescues) {
                Message message = (Message) IokeObject.data(ioke.reportMessage);
                String errorMessage = message.sendTo(ioke.reportMessage, ioke.ground, e.getCondition()).toString();
                throw new AssertionError(errorMessage);
            } else {
                throw e;
            }
        } finally {
            ioke.unregisterRescues(failureRescues);
            ioke.unregisterRescues(pendingRescues);
        }

    }

    private Object multilineArg(Object[] args) {
        Object multilineArg;
        if (args.length > 0) {
            if (args[args.length - 1] instanceof String) {
                multilineArg = ioke.newText((String) args[args.length - 1]);
            } else if (args[args.length - 1] instanceof Table) {
                multilineArg = args[args.length - 1];
            } else {
                multilineArg = ioke.nil;
            }
        } else {
            multilineArg = ioke.nil;
        }
        return multilineArg;
    }

    Object invoke(IokeObject iokeStepDefObject, String message, Object... args) throws ControlFlow {
        IokeObject msg = ioke.newMessage(message);
        Message m = (Message) IokeObject.data(msg);
        return m.sendTo(msg, iokeStepDefObject, iokeStepDefObject, Arrays.asList(args));
    }


}
