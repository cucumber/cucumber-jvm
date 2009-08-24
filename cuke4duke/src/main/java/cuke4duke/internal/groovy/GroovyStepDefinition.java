package cuke4duke.internal.groovy;

import cuke4duke.internal.language.StepDefinition;
import groovy.lang.Closure;
import org.jruby.Ruby;
import org.jruby.RubyArray;
import org.jruby.RubyRegexp;

import java.util.regex.Pattern;

public class GroovyStepDefinition implements StepDefinition {
    private final GroovyLanguage groovyLanguage;
    private final Pattern pattern;
    private final Closure body;


    public GroovyStepDefinition(GroovyLanguage groovyLanguage, Pattern pattern, Closure body) {
        this.groovyLanguage = groovyLanguage;
        this.pattern = pattern;
        this.body = body;
    }

    GroovyStepDefinition(GroovyLanguage groovyLanguage, String pattern, Closure body) {
        this(groovyLanguage, java.util.regex.Pattern.compile(pattern), body);
    }

    public RubyRegexp regexp() {
        // TODO: Translate and pass correct flags.
        return RubyRegexp.newRegexp(Ruby.getGlobalRuntime(), pattern.pattern(), RubyRegexp.RE_OPTION_LONGEST);
    }

    public String file_colon_line() {
        return body.toString();
    }

    public void invoke(RubyArray args) {
        groovyLanguage.invokeClosure(body, args);
    }
}
