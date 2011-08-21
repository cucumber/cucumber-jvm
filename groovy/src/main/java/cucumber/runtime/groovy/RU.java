package cucumber.runtime.groovy;

import groovy.lang.Closure;

import java.util.regex.Pattern;

public class RU {
    public static void Допустим(Pattern regexp, Closure body) throws Throwable {
        GroovyBackend.addStepDefinition(regexp, body);
    }

    public static void Дано(Pattern regexp, Closure body) throws Throwable {
        GroovyBackend.addStepDefinition(regexp, body);
    }

    public static void Пусть(Pattern regexp, Closure body) throws Throwable {
        GroovyBackend.addStepDefinition(regexp, body);
    }

    public static void Если(Pattern regexp, Closure body) throws Throwable {
        GroovyBackend.addStepDefinition(regexp, body);
    }

    public static void Когда(Pattern regexp, Closure body) throws Throwable {
        GroovyBackend.addStepDefinition(regexp, body);
    }

    public static void То(Pattern regexp, Closure body) throws Throwable {
        GroovyBackend.addStepDefinition(regexp, body);
    }

    public static void Тогда(Pattern regexp, Closure body) throws Throwable {
        GroovyBackend.addStepDefinition(regexp, body);
    }

    public static void И(Pattern regexp, Closure body) throws Throwable {
        GroovyBackend.addStepDefinition(regexp, body);
    }

    public static void Ктомуже(Pattern regexp, Closure body) throws Throwable {
        GroovyBackend.addStepDefinition(regexp, body);
    }

    public static void Но(Pattern regexp, Closure body) throws Throwable {
        GroovyBackend.addStepDefinition(regexp, body);
    }

    public static void А(Pattern regexp, Closure body) throws Throwable {
        GroovyBackend.addStepDefinition(regexp, body);
    }

}
