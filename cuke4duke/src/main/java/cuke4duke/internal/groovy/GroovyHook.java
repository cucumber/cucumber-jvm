package cuke4duke.internal.groovy;

import cuke4duke.internal.language.AbstractHook;
import groovy.lang.Closure;
import org.jruby.runtime.builtin.IRubyObject;

public class GroovyHook extends AbstractHook {
    private final GroovyLanguage groovyLanguage;
    private final Closure body;

    public GroovyHook(String[] tagExpressions, Closure body, GroovyLanguage groovyLanguage) {
        super(tagExpressions);
        this.groovyLanguage = groovyLanguage;
        this.body = body;
    }

    public void invoke(String location, IRubyObject scenario) {
        groovyLanguage.invokeClosure(body, new Object[]{scenario});
    }

}
