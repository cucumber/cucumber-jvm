package cucumber.runtime.groovy;

import groovy.lang.Closure;

public class Hooks {
    public static void World(Closure body) throws Throwable {
        GroovyBackend.registerWorld(body);
    }
}
