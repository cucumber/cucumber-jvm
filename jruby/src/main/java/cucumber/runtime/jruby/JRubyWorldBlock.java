package cucumber.runtime.jruby;

import org.jruby.RubyObject;
import org.jruby.runtime.builtin.IRubyObject;

public class JRubyWorldBlock {

    private final RubyObject block;

    public JRubyWorldBlock(RubyObject block) {
        this.block = block;
    }

    public void execute() {
        IRubyObject[] jrubyArgs = new IRubyObject[0];
        block.callMethod("execute", jrubyArgs);
    }
}
