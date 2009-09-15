package cuke4duke.internal.groovy;

import cuke4duke.internal.JRuby;
import cuke4duke.internal.language.StepDefinition;
import groovy.lang.Closure;
import org.jruby.RubyArray;
import org.jruby.RubyRegexp;

public class GroovyStepDefinition implements StepDefinition {
    private final GroovyLanguage groovyLanguage;
    private final String regexp;
    private final Closure body;


    public GroovyStepDefinition(GroovyLanguage groovyLanguage, String regexp, Closure body) {
        this.groovyLanguage = groovyLanguage;
        this.regexp = regexp;
        this.body = body;
    }

    public RubyRegexp regexp() {
        return RubyRegexp.newRegexp(JRuby.getRuntime(), regexp, RubyRegexp.RE_OPTION_LONGEST);
    }

    public String file_colon_line() {
        return body.toString();
    }

    public void invoke(RubyArray args) {
        groovyLanguage.invokeClosure(body, args);
    }
}
