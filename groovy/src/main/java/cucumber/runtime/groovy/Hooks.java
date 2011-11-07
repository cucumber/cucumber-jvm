package cucumber.runtime.groovy;

import gherkin.TagExpression;
import groovy.lang.Closure;

import static java.util.Arrays.asList;

public class Hooks {
    public static void World(Closure body) throws Throwable {
        GroovyBackend.instance.registerWorld(body);
    }

    public static void Before(Object... tagsExpressionsAndBody) throws Throwable {
        addHook(tagsExpressionsAndBody, true);
    }

    public static void After(Object... tagsExpressionsAndBody) throws Throwable {
        addHook(tagsExpressionsAndBody, false);
    }

    private static void addHook(Object[] tagsExpressionsAndBody, boolean before) {
        String[] tagExpressions = new String[tagsExpressionsAndBody.length - 1];
        System.arraycopy(tagsExpressionsAndBody, 0, tagExpressions, 0, tagExpressions.length);
        TagExpression tagExpression = new TagExpression(asList(tagExpressions));
        Closure body = (Closure) tagsExpressionsAndBody[tagsExpressionsAndBody.length - 1];
        if (before) {
            GroovyBackend.instance.addBeforeHook(tagExpression, body);
        } else {
            GroovyBackend.instance.addAfterHook(tagExpression, body);
        }
    }
}
