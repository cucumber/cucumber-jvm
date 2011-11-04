package cucumber.runtime.jruby;


import cucumber.runtime.HookDefinition;
import cucumber.runtime.ScenarioResult;
import gherkin.TagExpression;
import org.jruby.RubyObject;
import org.jruby.runtime.builtin.IRubyObject;

import java.util.Collection;

import static java.util.Arrays.asList;

public class JRubyHookDefinition implements HookDefinition {

    private RubyObject hook;
    private int order;
    private final TagExpression tagExpression;

    public JRubyHookDefinition(String[] tagExpressions, RubyObject hook) {
        tagExpression = new TagExpression(asList(tagExpressions));
        this.order = 0;
        this.hook = hook;
    }

    @Override
    public void execute(ScenarioResult scenarioResult) throws Throwable {
        IRubyObject[] jrybyArgs = new IRubyObject[0];
        hook.callMethod("execute", jrybyArgs);
    }

    @Override
    public boolean matches(Collection<String> tags) {
        return tagExpression.eval(tags);
    }

    @Override
    public int getOrder() {
        return order;
    }

}
