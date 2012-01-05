package cucumber.runtime.groovy;

import groovy.lang.Closure;

import java.util.regex.Pattern;

public class SK {

    public static void A(Pattern regexp, Closure body) throws Throwable {
        GroovyBackend.instance.addStepDefinition(regexp, body);
    }

    public static void Ale(Pattern regexp, Closure body) throws Throwable {
        GroovyBackend.instance.addStepDefinition(regexp, body);
    }

    public static void Keď(Pattern regexp, Closure body) throws Throwable {
        GroovyBackend.instance.addStepDefinition(regexp, body);
    }

    public static void Pokiaľ(Pattern regexp, Closure body) throws Throwable {
        GroovyBackend.instance.addStepDefinition(regexp, body);
    }

    public static void Tak(Pattern regexp, Closure body) throws Throwable {
        GroovyBackend.instance.addStepDefinition(regexp, body);
    }

}
