package cucumber.runtime.groovy;

import cuke4duke.internal.Utils;
import groovy.lang.Closure;
import java.util.Locale;
import java.util.regex.Pattern;

public class FR {
    private final static Locale locale = Utils.localeFor("fr");
    public static void Soit(Pattern regexp, Closure body) throws Throwable {
        GroovyBackend.addStepDefinition(regexp, body, locale);
    }

    public static void Etantdonné(Pattern regexp, Closure body) throws Throwable {
        GroovyBackend.addStepDefinition(regexp, body, locale);
    }

    public static void Quand(Pattern regexp, Closure body) throws Throwable {
        GroovyBackend.addStepDefinition(regexp, body, locale);
    }

    public static void Lorsque(Pattern regexp, Closure body) throws Throwable {
        GroovyBackend.addStepDefinition(regexp, body, locale);
    }

    public static void Lorsqu(Pattern regexp, Closure body) throws Throwable {
        GroovyBackend.addStepDefinition(regexp, body, locale);
    }

    public static void Alors(Pattern regexp, Closure body) throws Throwable {
        GroovyBackend.addStepDefinition(regexp, body, locale);
    }

    public static void Et(Pattern regexp, Closure body) throws Throwable {
        GroovyBackend.addStepDefinition(regexp, body, locale);
    }

    public static void Mais(Pattern regexp, Closure body) throws Throwable {
        GroovyBackend.addStepDefinition(regexp, body, locale);
    }

}
