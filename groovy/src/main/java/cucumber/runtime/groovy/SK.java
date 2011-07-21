package cucumber.runtime.groovy;

import cuke4duke.internal.Utils;
import groovy.lang.Closure;
import java.util.Locale;
import java.util.regex.Pattern;

public class SK {
    private final static Locale locale = Utils.localeFor("sk");
    public static void Pokiaľ(Pattern regexp, Closure body) throws Throwable {
        GroovyBackend.addStepDefinition(regexp, body, locale);
    }

    public static void Keď(Pattern regexp, Closure body) throws Throwable {
        GroovyBackend.addStepDefinition(regexp, body, locale);
    }

    public static void Tak(Pattern regexp, Closure body) throws Throwable {
        GroovyBackend.addStepDefinition(regexp, body, locale);
    }

    public static void A(Pattern regexp, Closure body) throws Throwable {
        GroovyBackend.addStepDefinition(regexp, body, locale);
    }

    public static void Ale(Pattern regexp, Closure body) throws Throwable {
        GroovyBackend.addStepDefinition(regexp, body, locale);
    }

}
