package cucumber.runtime.groovy;

import groovy.lang.Closure;

import java.util.regex.Pattern;

public class Dsl {
    static GroovyBackend backend;

    public static void Given(Pattern regexp, Closure body) throws Throwable {
        registerStepDefinition(regexp, body);
    }

    public static void When(Pattern regexp, Closure body) throws Throwable {
        registerStepDefinition(regexp, body);
    }

    public static void Then(Pattern regexp, Closure body) throws Throwable {
        registerStepDefinition(regexp, body);
    }

    public static void World(Closure body) throws Throwable {
        backend.registerWorld(body);
    }

    private static void registerStepDefinition(Pattern regexp, Closure body) {
        StackTraceElement location = stepDefLocation();
        backend.addStepDefinition(regexp, body, location);
    }

    private static StackTraceElement stepDefLocation() {
        Throwable t = new Throwable();
        StackTraceElement[] stackTraceElements = t.getStackTrace();
        for (StackTraceElement stackTraceElement : stackTraceElements) {
            if(stackTraceElement.getFileName().endsWith(".groovy")) {
                return stackTraceElement;
            }
        }
        throw new RuntimeException("Couldn't find location for step definition");
    }
}
