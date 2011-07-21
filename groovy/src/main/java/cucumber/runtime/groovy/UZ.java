package cucumber.runtime.groovy;

import cuke4duke.internal.Utils;
import groovy.lang.Closure;
import java.util.Locale;
import java.util.regex.Pattern;

public class UZ {
    private final static Locale locale = Utils.localeFor("uz");
    public static void Агар(Pattern regexp, Closure body) throws Throwable {
        GroovyBackend.addStepDefinition(regexp, body, locale);
    }

    public static void Унда(Pattern regexp, Closure body) throws Throwable {
        GroovyBackend.addStepDefinition(regexp, body, locale);
    }

    public static void Ва(Pattern regexp, Closure body) throws Throwable {
        GroovyBackend.addStepDefinition(regexp, body, locale);
    }

    public static void Лекин(Pattern regexp, Closure body) throws Throwable {
        GroovyBackend.addStepDefinition(regexp, body, locale);
    }

    public static void Бирок(Pattern regexp, Closure body) throws Throwable {
        GroovyBackend.addStepDefinition(regexp, body, locale);
    }

    public static void Аммо(Pattern regexp, Closure body) throws Throwable {
        GroovyBackend.addStepDefinition(regexp, body, locale);
    }

}
