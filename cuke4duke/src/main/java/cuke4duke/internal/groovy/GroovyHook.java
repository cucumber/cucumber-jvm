package cuke4duke.internal.groovy;

import cuke4duke.Scenario;
import cuke4duke.internal.language.AbstractHook;
import groovy.lang.Closure;

import java.util.List;
import java.util.Locale;

public class GroovyHook extends AbstractHook {
    private final GroovyLanguage groovyLanguage;
    private final Closure body;

    public GroovyHook(List<String> tagExpressions, Closure body, GroovyLanguage groovyLanguage) {
        super(tagExpressions);
        this.groovyLanguage = groovyLanguage;
        this.body = body;
    }

    public void invoke(String location, Scenario scenario) throws Throwable {
        groovyLanguage.invokeClosure(body, new Object[]{scenario}, Locale.getDefault());
    }
}
