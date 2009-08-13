package cuke4duke.internal;

import org.jruby.runtime.builtin.IRubyObject;
import org.jruby.javasupport.JavaEmbedUtils;
import cuke4duke.internal.language.Hook;
import cuke4duke.internal.language.StepDefinition;

public class StepMotherAdapter {
    private final IRubyObject stepMother;

    public StepMotherAdapter(IRubyObject stepMother) {
        this.stepMother = stepMother;
    }

    public void registerStepDefinition(StepDefinition stepDefinition) {
        invoke("register_step_definition", new Object[]{stepDefinition});
    }

    public void registerBefore(Hook hook) {
        invoke("register_hook", new Object[]{"before", hook});
    }

    public void registerAfter(Hook hook) {
        invoke("register_hook", new Object[]{"after", hook});
    }

    private void invoke(String method, Object[] args) {
        JavaEmbedUtils.invokeMethod(
                stepMother.getRuntime(),
                stepMother,
                method,
                args,
                IRubyObject.class);
    }
}
