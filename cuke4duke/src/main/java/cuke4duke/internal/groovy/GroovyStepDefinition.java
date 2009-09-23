package cuke4duke.internal.groovy;

import cuke4duke.internal.language.StepDefinition;
import cuke4duke.internal.language.StepArgument;
import cuke4duke.internal.language.JdkPatternArgumentMatcher;
import groovy.lang.Closure;
import org.jruby.RubyArray;

import java.util.regex.Pattern;
import java.util.List;

public class GroovyStepDefinition implements StepDefinition {
    private final GroovyLanguage groovyLanguage;
    private final Pattern regexp;
    private final Closure body;

    public GroovyStepDefinition(GroovyLanguage groovyLanguage, Pattern regexp, Closure body) {
        this.groovyLanguage = groovyLanguage;
        this.regexp = regexp;
        this.body = body;
    }

    public String regexp_source() {
        return regexp.pattern();
    }

    public String file_colon_line() {
        return body.toString();
    }

    public void invoke(RubyArray args) {
        groovyLanguage.invokeClosure(body, args);
    }

    public List<StepArgument> arguments_from(String stepName) {
        return JdkPatternArgumentMatcher.argumentsFrom(regexp, stepName);
    }
}
