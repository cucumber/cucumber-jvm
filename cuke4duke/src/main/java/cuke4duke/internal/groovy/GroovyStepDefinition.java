package cuke4duke.internal.groovy;

import cuke4duke.internal.language.StepDefinition;
import cuke4duke.internal.language.Group;
import cuke4duke.internal.language.JdkRegexpGroup;
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

    public String file_colon_line() {
        return body.toString();
    }

    public void invoke(RubyArray args) {
        groovyLanguage.invokeClosure(body, args);
    }

    public List<Group> groups(String stepName) {
        return JdkRegexpGroup.groupsFrom(regexp, stepName);
    }
}
