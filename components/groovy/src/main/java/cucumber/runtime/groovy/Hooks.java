package cucumber.runtime.groovy;

import groovy.lang.Closure;

import java.util.regex.Pattern;

public class Hooks {
    public static void World(Closure body) throws Throwable {
        GroovyBackend.registerWorld(body);
    }
}
