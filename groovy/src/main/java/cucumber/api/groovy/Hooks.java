package cucumber.api.groovy;

import cucumber.runtime.CucumberException;
import cucumber.runtime.groovy.GroovyBackend;
import groovy.lang.Closure;

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
        String tagExpression = null;
        Integer timeoutMillis = null;
        Closure body = null;

        for (Object o : tagsExpressionsAndBody) {
            if (o instanceof String) {
                if (tagExpression != null) throw new CucumberException("tagExpression already set");
                tagExpression = (String) o;
            } else if (o instanceof Integer) {
                if (tagExpression != null) throw new CucumberException("timeoutMillis already set");
                timeoutMillis = (Integer) o;
            } else if (o instanceof Closure) {
                if (tagExpression != null) throw new CucumberException("body already set");
                body = (Closure) o;
            }
        }

        if (before) {
            GroovyBackend.instance.addBeforeHook(tagExpression, timeoutMillis, body);
        } else {
            GroovyBackend.instance.addAfterHook(tagExpression, timeoutMillis, body);
        }
    }
}
