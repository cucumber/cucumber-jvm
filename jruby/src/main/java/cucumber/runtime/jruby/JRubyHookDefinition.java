package cucumber.runtime.jruby;

import cucumber.runtime.HookDefinition;
import cucumber.runtime.ScenarioResult;
import gherkin.TagExpression;
import gherkin.formatter.model.Tag;
import org.jruby.RubyObject;
import org.jruby.javasupport.JavaEmbedUtils;
import org.jruby.runtime.builtin.IRubyObject;

import java.util.Collection;

import static java.util.Arrays.asList;

public class JRubyHookDefinition implements HookDefinition {

    private final TagExpression tagExpression;
    private final RubyObject hook;

    public JRubyHookDefinition(String[] tagExpressions, RubyObject hook) {
        tagExpression = new TagExpression(asList(tagExpressions));
        this.hook = hook;
    }

    @Override
    public void execute(ScenarioResult scenarioResult) throws Throwable {
        IRubyObject[] jrybyArgs = new IRubyObject[1];
        jrybyArgs[0] = JavaEmbedUtils.javaToRuby(hook.getRuntime(), scenarioResult);
        hook.callMethod("execute", jrybyArgs);
    }

    @Override
    public boolean matches(Collection<Tag> tags) {
        return tagExpression.eval(tags);
    }

    @Override
    public int getOrder() {
        return 0;
    }

}
