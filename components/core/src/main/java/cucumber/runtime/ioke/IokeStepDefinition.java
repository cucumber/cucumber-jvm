package cucumber.runtime.ioke;

import cucumber.runtime.StepDefinition;
import cuke4duke.PyString;
import cuke4duke.Table;
import gherkin.formatter.Argument;
import gherkin.formatter.model.Step;
import ioke.lang.IokeObject;
import ioke.lang.Message;
import ioke.lang.Runtime;
import ioke.lang.exceptions.ControlFlow;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class IokeStepDefinition implements StepDefinition {
    private final Runtime ioke;
    private final IokeObject iokeStepDefObject;
    private final IokeBackend lang;
    private final String location;

    public IokeStepDefinition(IokeBackend iokeBackend, Runtime ioke, IokeObject iokeStepDefObject, String location) throws Throwable {
        this.ioke = ioke;
        this.iokeStepDefObject = iokeStepDefObject;
        this.lang = iokeBackend;
        this.location = location;
    }

    public String regexp_source() throws Throwable {
        return (String) invoke("regexp_source");
    }

    private Object multilineArg(Object[] args) {
        Object multilineArg;
        if (args.length > 0) {
            if (args[args.length - 1] instanceof PyString) {
                multilineArg = ioke.newText(((PyString) args[args.length - 1]).to_s());
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

    public List<Argument> matchedArguments(Step step) {
        try {
            Object args = invoke("arguments_from", step.getName());
            if(args.equals(ioke.nil)) {
                return null;
            } else {
                return (List<Argument>) args;
            }
        } catch (ControlFlow controlFlow) {
            throw new RuntimeException("Failed to get step args", controlFlow);
        }
    }

    public String getLocation() {
        return location;
    }

    public Class<?>[] getParameterTypes() {
        return new Class[]{Object.class};
    }

    public void execute(Object[] args) throws Throwable {
        List<Runtime.RescueInfo> pendingRescues = new ArrayList<Runtime.RescueInfo>();
        IokeObject rr = IokeObject.as(((Message) IokeObject.data(ioke.mimic)).sendTo(ioke.mimic, ioke.ground, ioke.rescue), ioke.ground);
        List<Object> conds = new ArrayList<Object>();
        conds.add(lang.pendingCondition);
        pendingRescues.add(new Runtime.RescueInfo(rr, conds, pendingRescues, ioke.getBindIndex()));
        ioke.registerRescues(pendingRescues);

        List<Runtime.RescueInfo> failureRescues = new ArrayList<Runtime.RescueInfo>();
        IokeObject rr2 = IokeObject.as(((Message) IokeObject.data(ioke.mimic)).sendTo(ioke.mimic, ioke.ground, ioke.rescue), ioke.ground);
        List<Object> failureConds = new ArrayList<Object>();
        failureConds.add(lang.failedExpectationCondition);
        failureRescues.add(new Runtime.RescueInfo(rr2, failureConds, failureRescues, ioke.getBindIndex()));
        ioke.registerRescues(failureRescues);

        try {
            invoke("invoke", multilineArg(args));
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

    public boolean isDefinedAt(StackTraceElement stackTraceElement) {
        return stackTraceElement.getClassName().equals(getClass().getName());
    }

    private Object invoke(String message, Object... args) throws ControlFlow {
        IokeObject msg = ioke.newMessage(message);
        Message m = (Message) IokeObject.data(msg);
        return m.sendTo(msg, iokeStepDefObject, iokeStepDefObject, Arrays.asList(args));
    }

}
