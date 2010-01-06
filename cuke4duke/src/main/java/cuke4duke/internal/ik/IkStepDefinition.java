package cuke4duke.internal.ik;

import cuke4duke.PyString;
import cuke4duke.Table;
import cuke4duke.internal.JRuby;
import cuke4duke.internal.language.AbstractStepDefinition;
import cuke4duke.internal.language.StepArgument;
import ioke.lang.IokeObject;
import ioke.lang.Message;
import ioke.lang.Runtime;
import ioke.lang.exceptions.ControlFlow;

import java.util.ArrayList;
import java.util.List;

public class IkStepDefinition extends AbstractStepDefinition {
    private final Runtime ioke;
    private final IokeObject iokeStepDefObject;
    private String regexpSource;
    private final IkLanguage lang;

    public static Object throwCucumberIokeException(String message) {
        throw JRuby.error("IokeException", message);
    }

    public IkStepDefinition(IkLanguage ikLanguage, Runtime ioke, IokeObject iokeStepDefObject) throws Throwable {
        super(ikLanguage);
        this.ioke = ioke;
        this.iokeStepDefObject = iokeStepDefObject;
        this.lang = ikLanguage;
        register();
    }

    public Object invokeWithArgs(Object[] args) throws Throwable {
        IokeObject msg = ioke.newMessage("invoke");
        Message invoke = (Message) IokeObject.data(msg);

        List<Runtime.RescueInfo> pendingRescues = new ArrayList<Runtime.RescueInfo>();
        IokeObject rr = IokeObject.as(((Message)IokeObject.data(ioke.mimic)).sendTo(ioke.mimic, ioke.ground, ioke.rescue), ioke.ground);
        List<Object> conds = new ArrayList<Object>();
        conds.add(lang.pendingCondition);
        pendingRescues.add(new Runtime.RescueInfo(rr, conds, pendingRescues, ioke.getBindIndex()));
        ioke.registerRescues(pendingRescues);


        List<Runtime.RescueInfo> failureRescues = new ArrayList<Runtime.RescueInfo>();
        IokeObject rr2 = IokeObject.as(((Message)IokeObject.data(ioke.mimic)).sendTo(ioke.mimic, ioke.ground, ioke.rescue), ioke.ground);
        List<Object> failureConds = new ArrayList<Object>();
        failureConds.add(lang.failedExpectationCondition);
        failureRescues.add(new Runtime.RescueInfo(rr2, failureConds, failureRescues, ioke.getBindIndex()));
        ioke.registerRescues(failureRescues);

        try {
	        return invoke.sendTo(msg, iokeStepDefObject, iokeStepDefObject, multilineArg(args));
        } catch(ControlFlow.Rescue e) {
            if(e.getRescue().token == pendingRescues) {
                throw JRuby.cucumberPending("TODO");
            } else if(e.getRescue().token == failureRescues) {
                return throwCucumberIokeException(((Message)IokeObject.data(ioke.reportMessage)).sendTo(ioke.reportMessage, ioke.ground, e.getCondition()).toString());
            } else {
                throw e;
            }
        } finally {
            ioke.unregisterRescues(failureRescues);
            ioke.unregisterRescues(pendingRescues);
        }
    }

    public String regexp_source() throws Throwable {
        if(regexpSource == null) findRegexpSource();
        return regexpSource;
    }

    public String file_colon_line() throws Throwable {
        return regexp_source();
    }

    @SuppressWarnings("unchecked")
    public List<StepArgument> arguments_from(String stepName) throws Throwable {
        IokeObject msg = ioke.newMessage("arguments_from");
        Message arguments_from = (Message) IokeObject.data(msg);
        Object args = arguments_from.sendTo(msg, iokeStepDefObject, iokeStepDefObject, stepName);
        if(args instanceof List<?>) {
            return (List<StepArgument>) args;
        } else {
            return null;
        }
    }

    private void findRegexpSource() throws ControlFlow {
        IokeObject msg = ioke.newMessage("regexp_source");
        Message regexp_source = (Message) IokeObject.data(msg);
        regexpSource = regexp_source.sendTo(msg, iokeStepDefObject, iokeStepDefObject).toString();
    }

    private Object multilineArg(Object[] args) {
        Object multilineArg;
        if(args.length > 0) {
            if(args[args.length-1] instanceof PyString) {
                multilineArg = ioke.newText(((PyString) args[args.length-1]).to_s());
            } else if(args[args.length-1] instanceof Table) {
                multilineArg = args[args.length-1];
            } else {
                multilineArg = ioke.nil;
            }
        } else {
            multilineArg = ioke.nil;
        }
        return multilineArg;
    }

}
