package cucumber.runtime.groovy;

import groovy.lang.Closure;

import java.util.regex.Pattern;

public class TR {

    public static void Ama(Pattern regexp, Closure body) throws Throwable {
        GroovyBackend.instance.addStepDefinition(regexp, body);
    }

    public static void Diyelimki(Pattern regexp, Closure body) throws Throwable {
        GroovyBackend.instance.addStepDefinition(regexp, body);
    }

    public static void Eğerki(Pattern regexp, Closure body) throws Throwable {
        GroovyBackend.instance.addStepDefinition(regexp, body);
    }

    public static void Fakat(Pattern regexp, Closure body) throws Throwable {
        GroovyBackend.instance.addStepDefinition(regexp, body);
    }

    public static void Ozaman(Pattern regexp, Closure body) throws Throwable {
        GroovyBackend.instance.addStepDefinition(regexp, body);
    }

    public static void Ve(Pattern regexp, Closure body) throws Throwable {
        GroovyBackend.instance.addStepDefinition(regexp, body);
    }

}
