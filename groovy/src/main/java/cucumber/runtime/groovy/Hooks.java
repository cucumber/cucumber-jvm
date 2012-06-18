package cucumber.runtime.groovy;

import gherkin.TagExpression;
import groovy.lang.Closure;

import java.util.ArrayList;
import java.util.List;

public class Hooks {
    public static void World(Closure body) throws Throwable {
        GroovyBackend.instance.registerWorld(body);
    }

    public static void Before(Object... args) throws Throwable {
        addHook(args, true);
    }

    public static void After(Object... args) throws Throwable {
        addHook(args, false);
    }

    private static void addHook(Object[] tagsExpressionsAndBody, boolean before) {
        List<String> tagExpressions = new ArrayList<String>();
        int timeoutMillis = 0;
        Closure body = null;

        for (Object o : tagsExpressionsAndBody) {
            if (o instanceof String) {
                tagExpressions.add((String) o);
            } else if (o instanceof Integer) {
                timeoutMillis = (Integer) o;
            } else if (o instanceof Closure) {
                body = (Closure) o;
            }
        }

        TagExpression tagExpression = new TagExpression(tagExpressions);
        if (before) {
            GroovyBackend.instance.addBeforeHook(tagExpression, timeoutMillis, body);
        } else {
            GroovyBackend.instance.addAfterHook(tagExpression, timeoutMillis, body);
        }
    }
}
