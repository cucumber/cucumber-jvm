package cucumber.runtime.jruby;

import cucumber.api.Scenario;
import cucumber.runtime.HookDefinition;
import org.jruby.RubyObject;

import java.util.List;

public class JRubyHookDefinition implements HookDefinition {

    private final String tagExpression;
    private final RubyObject hookRunner;
    private String file;
    private Long line;
    private final JRubyBackend jRubyBackend;

    public JRubyHookDefinition(JRubyBackend jRubyBackend, String tagExpression, RubyObject hookRunner) {
        this.jRubyBackend = jRubyBackend;
        this.tagExpression = tagExpression;
        this.hookRunner = hookRunner;
    }

    @Override
    public String getLocation(boolean detail) {
        if (file == null) {
            List fileAndLine = (List) hookRunner.callMethod("file_and_line").toJava(List.class);
            file = (String) fileAndLine.get(0);
            line = (Long) fileAndLine.get(1);
        }
        return file + ":" + line;
    }

    @Override
    public void execute(Scenario scenario) throws Throwable {
        jRubyBackend.executeHook(hookRunner, scenario);
    }

    @Override
    public String getTagExpression() {
        return tagExpression;
    }

    @Override
    public int getOrder() {
        return 0;
    }

}
