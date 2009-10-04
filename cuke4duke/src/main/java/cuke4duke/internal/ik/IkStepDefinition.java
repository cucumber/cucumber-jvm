package cuke4duke.internal.ik;

import cuke4duke.internal.language.AbstractStepDefinition;
import cuke4duke.internal.language.StepArgument;
import cuke4duke.internal.Utils;
import ioke.lang.IokeObject;
import ioke.lang.Message;
import ioke.lang.Runtime;

import java.util.List;

public class IkStepDefinition extends AbstractStepDefinition {
    private final Runtime ioke;
    private final IokeObject iokeStepDefObject;

    public IkStepDefinition(IkLanguage ikLanguage, Runtime ioke, IokeObject iokeStepDefObject) {
        super(ikLanguage);
        this.ioke = ioke;
        this.iokeStepDefObject = iokeStepDefObject;
    }

    protected Class<?>[] getParameterTypes(Object[] args) {
        return Utils.objectClassArray(args.length);
    }

    public void invokeWithJavaArgs(Object[] args) throws Throwable {
        IokeObject msg = ioke.newMessage("invoke");
        Message invoke = (Message) IokeObject.data(msg);
        // We could pass the args, but I don't think it's needed. The ioke stepdef code block
        // will access the arguments by group name instead. We just need to figure out a way to
        // make them available.
        invoke.sendTo(msg, iokeStepDefObject, iokeStepDefObject);
    }

    public String regexp_source() {
        return "/NOT_IMPLEMENTED/";
    }

    public String file_colon_line() {
        return "NOT_IMPLEMENTED:-1";
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
}
