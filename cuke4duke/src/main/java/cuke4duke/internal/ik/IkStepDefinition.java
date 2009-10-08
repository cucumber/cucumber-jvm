package cuke4duke.internal.ik;

import cuke4duke.internal.language.AbstractStepDefinition;
import cuke4duke.internal.language.StepArgument;
import cuke4duke.internal.Utils;
import cuke4duke.Table;
import ioke.lang.IokeObject;
import ioke.lang.Message;
import ioke.lang.Runtime;
import ioke.lang.IokeData;
import ioke.lang.exceptions.ControlFlow;

import java.util.List;

public class IkStepDefinition extends AbstractStepDefinition {
    private final Runtime ioke;
    private final IokeObject iokeStepDefObject;
    private String regexpSource;

    public IkStepDefinition(IkLanguage ikLanguage, Runtime ioke, IokeObject iokeStepDefObject) throws Throwable {
        super(ikLanguage);
        this.ioke = ioke;
        this.iokeStepDefObject = iokeStepDefObject;
        register();
    }

    protected Class<?>[] getParameterTypes(Object[] args) {
        return Utils.objectClassArray(args.length);
    }

    public void invokeWithJavaArgs(Object[] args) throws Throwable {
        IokeObject msg = ioke.newMessage("invoke");
        Message invoke = (Message) IokeObject.data(msg);

        // TODO: Change Cucumber API so that we get an additional argument
        // telling us whether or not we have a multiline argument. Needed
        // to support multiline Strings.
        if(args[args.length-1] instanceof Table) {
            invoke.sendTo(msg, iokeStepDefObject, iokeStepDefObject, args[args.length-1]);
        } else {
            invoke.sendTo(msg, iokeStepDefObject, iokeStepDefObject, IokeData.Nil);
        }
    }

    public String regexp_source() throws Throwable {
        if(regexpSource == null) findRegexpSource();
        return regexpSource;
    }

    public String file_colon_line() throws Throwable {
        return regexp_source();
    }

    public List<StepArgument> arguments_from(String stepName) throws Throwable {
        IokeObject msg = ioke.newMessage("arguments_from");
        Message arguments_from = (Message) IokeObject.data(msg);
        Object args = arguments_from.sendTo(msg, iokeStepDefObject, iokeStepDefObject, stepName);
        if(args instanceof List) {
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

}
