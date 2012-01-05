package cucumber.runtime.groovy;

import groovy.lang.Closure;

import java.util.regex.Pattern;

public class DE {

    public static void Aber(Pattern regexp, Closure body) throws Throwable {
        GroovyBackend.instance.addStepDefinition(regexp, body);
    }

    public static void Angenommen(Pattern regexp, Closure body) throws Throwable {
        GroovyBackend.instance.addStepDefinition(regexp, body);
    }

    public static void Dann(Pattern regexp, Closure body) throws Throwable {
        GroovyBackend.instance.addStepDefinition(regexp, body);
    }

    public static void Gegebensei(Pattern regexp, Closure body) throws Throwable {
        GroovyBackend.instance.addStepDefinition(regexp, body);
    }

    public static void Und(Pattern regexp, Closure body) throws Throwable {
        GroovyBackend.instance.addStepDefinition(regexp, body);
    }

    public static void Wenn(Pattern regexp, Closure body) throws Throwable {
        GroovyBackend.instance.addStepDefinition(regexp, body);
    }

}
