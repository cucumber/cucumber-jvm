package cucumber.runtime.groovy;

import cuke4duke.internal.Utils;
import groovy.lang.Closure;
import java.util.Locale;
import java.util.regex.Pattern;

public class KO {
    private final static Locale locale = Utils.localeFor("ko");
    public static void 조건(Pattern regexp, Closure body) throws Throwable {
        GroovyBackend.addStepDefinition(regexp, body, locale);
    }

    public static void 먼저(Pattern regexp, Closure body) throws Throwable {
        GroovyBackend.addStepDefinition(regexp, body, locale);
    }

    public static void 만일(Pattern regexp, Closure body) throws Throwable {
        GroovyBackend.addStepDefinition(regexp, body, locale);
    }

    public static void 만약(Pattern regexp, Closure body) throws Throwable {
        GroovyBackend.addStepDefinition(regexp, body, locale);
    }

    public static void 그러면(Pattern regexp, Closure body) throws Throwable {
        GroovyBackend.addStepDefinition(regexp, body, locale);
    }

    public static void 그리고(Pattern regexp, Closure body) throws Throwable {
        GroovyBackend.addStepDefinition(regexp, body, locale);
    }

    public static void 하지만(Pattern regexp, Closure body) throws Throwable {
        GroovyBackend.addStepDefinition(regexp, body, locale);
    }

    public static void 단(Pattern regexp, Closure body) throws Throwable {
        GroovyBackend.addStepDefinition(regexp, body, locale);
    }

}
