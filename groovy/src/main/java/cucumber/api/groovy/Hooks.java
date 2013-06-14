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

    private static void addHook(Object[] args, boolean before) {
        String tagExpression = null;
        Integer timeoutMillis = null;
        Closure body = null;

        for (Object o : args) {
            if (o instanceof String) {
                if (tagExpression != null) throw new CucumberException(String.format("tagExpression already set to %s. Can't set it to %s", tagExpression, o));
                tagExpression = (String) o;
            } else if (o instanceof Integer) {
                if (timeoutMillis != null) throw new CucumberException(String.format("timeoutMillis already set to %d. Can't set it to %d", String.valueOf(timeoutMillis), o));
                timeoutMillis = (Integer) o;
            } else if (o instanceof Closure) {
                if (body != null) throw new CucumberException("body already set");
                body = (Closure) o;
            }
        }

        if(timeoutMillis == null) {
            timeoutMillis = 0;
        }
        if (before) {
            GroovyBackend.instance.addBeforeHook(tagExpression, timeoutMillis, body);
        } else {
            GroovyBackend.instance.addAfterHook(tagExpression, timeoutMillis, body);
        }
    }
}
