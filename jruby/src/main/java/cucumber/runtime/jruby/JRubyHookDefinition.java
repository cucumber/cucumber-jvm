package cucumber.runtime.jruby;

import cucumber.api.Scenario;
import cucumber.runtime.HookDefinition;
import gherkin.TagExpression;
import gherkin.formatter.model.Tag;
import org.jruby.RubyObject;

import java.util.Collection;
import java.util.List;

import static java.util.Arrays.asList;

public class JRubyHookDefinition implements HookDefinition<Scenario> {

    private final TagExpression tagExpression;
    private final RubyObject hookRunner;
    private String file;
    private Long line;
    private final JRubyBackend jRubyBackend;

    public JRubyHookDefinition(JRubyBackend jRubyBackend, String[] tagExpressions, RubyObject hookRunner) {
        this.jRubyBackend = jRubyBackend;
        this.tagExpression = new TagExpression(asList(tagExpressions));
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
    public boolean matches(Collection<Tag> tags) {
        return tagExpression.evaluate(tags);
    }

    @Override
    public int getOrder() {
        return 0;
    }

    @Override
    public boolean isScenarioScoped() {
        return false;
    }
}
