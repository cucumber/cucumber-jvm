package cucumber.runtime.groovy;

import groovy.lang.Closure;

import java.util.regex.Pattern;

public class PL {
    public static void Zakładając(Pattern regexp, Closure body) throws Throwable {
        GroovyBackend.addStepDefinition(regexp, body);
    }

    public static void Mając(Pattern regexp, Closure body) throws Throwable {
        GroovyBackend.addStepDefinition(regexp, body);
    }

    public static void Jeżeli(Pattern regexp, Closure body) throws Throwable {
        GroovyBackend.addStepDefinition(regexp, body);
    }

    public static void Jeśli(Pattern regexp, Closure body) throws Throwable {
        GroovyBackend.addStepDefinition(regexp, body);
    }

    public static void Wtedy(Pattern regexp, Closure body) throws Throwable {
        GroovyBackend.addStepDefinition(regexp, body);
    }

    public static void Oraz(Pattern regexp, Closure body) throws Throwable {
        GroovyBackend.addStepDefinition(regexp, body);
    }

    public static void I(Pattern regexp, Closure body) throws Throwable {
        GroovyBackend.addStepDefinition(regexp, body);
    }

    public static void Ale(Pattern regexp, Closure body) throws Throwable {
        GroovyBackend.addStepDefinition(regexp, body);
    }

}
