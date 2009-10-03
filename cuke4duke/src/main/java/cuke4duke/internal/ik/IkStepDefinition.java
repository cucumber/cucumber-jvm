package cuke4duke.internal.ik;

import cuke4duke.internal.language.AbstractStepDefinition;
import cuke4duke.internal.language.StepArgument;
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
        throw new RuntimeException("Not implemented");
    }

    public void invokeWithJavaArgs(Object[] args) throws Throwable {
        throw new RuntimeException("Not implemented");
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
