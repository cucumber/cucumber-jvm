package cuke4duke.internal.groovy;

import groovy.lang.Closure;
import cuke4duke.internal.language.AbstractHook;

import java.util.List;

import org.jruby.RubyArray;
import org.jruby.Ruby;
import org.jruby.runtime.builtin.IRubyObject;

public class GroovyHook extends AbstractHook {
    private final GroovyLanguage groovyLanguage;
    private final Closure body;

    public GroovyHook(List<String> tagNames, Closure body, GroovyLanguage groovyLanguage) {
        super(tagNames);
        this.groovyLanguage = groovyLanguage;
        this.body = body;
    }

    public void invoke(String location, IRubyObject scenario) {
        RubyArray args = RubyArray.newArray(Ruby.getGlobalRuntime());
        args.add(null); // Needed because groovy closures always seem to have arity 1(??)
        groovyLanguage.invokeClosure(body, args);
    }

}
