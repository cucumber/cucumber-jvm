package cucumber.runtime.jruby;

import org.jruby.RubyObject;
import org.jruby.runtime.builtin.IRubyObject;

public class JRubyWorldDefinition {

    private final RubyObject worldRunner;

    public JRubyWorldDefinition(RubyObject worldRunner) {
        this.worldRunner = worldRunner;
    }

    public RubyObject execute(RubyObject currentWorld) {
        IRubyObject[] jrubyArgs = new IRubyObject[]{currentWorld};
        return (RubyObject) worldRunner.callMethod("execute", jrubyArgs);
    }
}
