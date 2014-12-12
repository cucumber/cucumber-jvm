package cucumber.api.groovy;

import cucumber.runtime.groovy.GroovyBackend;
import gherkin.TagExpression;
import groovy.lang.Closure;

import java.util.ArrayList;
import java.util.List;

public class Hooks {
    private static final int DEFAULT_ORDER = 10000;
    private static final long DEFAULT_TIMEOUT = 0;

    public static void World(Closure body) throws Throwable {
        GroovyBackend.getInstance().registerWorld(body);
    }

    /**
     * Registers a before hook, which is executed before specific, or all, scenarios.
     * <p/>
     * Following values are expected as hook parameters.
     * - Long timeoutMillis: max amount of milliseconds this is allowed to run for. The default is 0 which means no restriction.
     * - Integer order: the order in which this hook should run. Lower numbers are run first. The default is 10000.
     * - String(s) tags: one or more tag expression to filter the certain scenarios. The default is empty.
     * - Closure body: hook body which is executed before scenario. Not null.
     *
     * @param args the hook parameters
     */
    public static void Before(Object... args) {
        addHook(args, true);
    }

    /**
     * Registers an after hook, which is executed after specific, or all, scenarios.
     * <p/>
     * Following values are expected as hook parameters.
     * - Long timeoutMillis: max amount of milliseconds this is allowed to run for. The default is 0 which means no restriction.
     * - Integer order: the order in which this hook should run. Higher numbers are run first. The default is 10000.
     * - String(s) tags: one or more tag expression to filter the certain scenarios. The default is empty.
     * - Closure body: hook body which is executed after scenario. Not null.
     *
     * @param args the hook parameters
     */
    public static void After(Object... args) {
        addHook(args, false);
    }

    private static void addHook(Object[] tagsExpressionsAndBody, boolean before) {
        long timeoutMillis = DEFAULT_TIMEOUT;
        int order = DEFAULT_ORDER;
        Closure body = null;
        List<String> tagExpressions = new ArrayList<String>();

        for (Object o : tagsExpressionsAndBody) {
            if (o instanceof String) {
                tagExpressions.add((String) o);
            } else if (o instanceof Long) {
                timeoutMillis = (Long) o;
            } else if (o instanceof Integer) {
                order = (Integer) o;
            } else if (o instanceof Closure) {
                body = (Closure) o;
            }
        }

        TagExpression tagExpression = new TagExpression(tagExpressions);
        if (before) {
            GroovyBackend.getInstance().addBeforeHook(tagExpression, timeoutMillis, order, body);
        } else {
            GroovyBackend.getInstance().addAfterHook(tagExpression, timeoutMillis, order, body);
        }
    }
}
