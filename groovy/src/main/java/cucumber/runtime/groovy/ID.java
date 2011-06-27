package cucumber.runtime.groovy;

import groovy.lang.Closure;

import java.util.regex.Pattern;

public class ID {
    public static void Dengan(Pattern regexp, Closure body) throws Throwable {
        GroovyBackend.addStepDefinition(regexp, body);
    }

    public static void Ketika(Pattern regexp, Closure body) throws Throwable {
        GroovyBackend.addStepDefinition(regexp, body);
    }

    public static void Maka(Pattern regexp, Closure body) throws Throwable {
        GroovyBackend.addStepDefinition(regexp, body);
    }

    public static void Dan(Pattern regexp, Closure body) throws Throwable {
        GroovyBackend.addStepDefinition(regexp, body);
    }

    public static void Tapi(Pattern regexp, Closure body) throws Throwable {
        GroovyBackend.addStepDefinition(regexp, body);
    }

}
