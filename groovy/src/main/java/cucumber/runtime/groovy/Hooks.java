package cucumber.runtime.groovy;

import groovy.lang.Closure;

import java.util.Arrays;

import cucumber.runtime.HookDefinition;

public class Hooks {
    public static void World(Closure body) throws Throwable {
        GroovyBackend.registerWorld(body);
    }

    public static void Before(Object... tagsAndBody) throws Throwable {
        HookDefinition hook = createHook(tagsAndBody);
        if (hook != null) {
            GroovyBackend.addBeforeHook(hook);
        }
    }

    public static void After(Object... tagsAndBody) throws Throwable {
        HookDefinition hook = createHook(tagsAndBody);
        if (hook != null) {
            GroovyBackend.addAfterHook(hook);
        }
    }

    private static HookDefinition createHook(Object... tagsAndBody) {
        if (tagsAndBody.length == 0) return null;
        String[] tagNames = new String[tagsAndBody.length - 1];
        System.arraycopy(tagsAndBody, 0, tagNames, 0, tagNames.length);
        Closure body = (Closure)tagsAndBody[tagsAndBody.length - 1];
        return new GroovyHookDefinition(body, Arrays.asList(tagNames), Integer.MAX_VALUE);
    }
}
