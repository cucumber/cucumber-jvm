package cuke4duke.internal.groovy;

import cuke4duke.internal.language.AbstractHook;
import groovy.lang.Closure;
import org.jruby.runtime.builtin.IRubyObject;

import java.util.List;

public class GroovyHook extends AbstractHook {
    private final GroovyLanguage groovyLanguage;
    private final Closure body;

    public GroovyHook(List<String> tagNames, Closure body, GroovyLanguage groovyLanguage) {
        super(tagNames);
        this.groovyLanguage = groovyLanguage;
        this.body = body;
    }

    public void invoke(String location, IRubyObject scenario) {
        groovyLanguage.invokeClosure(body, new Object[]{scenario});
    }

}
