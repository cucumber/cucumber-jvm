package cucumber.runtime.groovy;

import cuke4duke.internal.Utils;
import groovy.lang.Closure;
import java.util.Locale;
import java.util.regex.Pattern;

public class ZH_TW {
    private final static Locale locale = Utils.localeFor("zh-TW");
    public static void 假設(Pattern regexp, Closure body) throws Throwable {
        GroovyBackend.addStepDefinition(regexp, body, locale);
    }

    public static void 當(Pattern regexp, Closure body) throws Throwable {
        GroovyBackend.addStepDefinition(regexp, body, locale);
    }

    public static void 那麼(Pattern regexp, Closure body) throws Throwable {
        GroovyBackend.addStepDefinition(regexp, body, locale);
    }

    public static void 而且(Pattern regexp, Closure body) throws Throwable {
        GroovyBackend.addStepDefinition(regexp, body, locale);
    }

    public static void 並且(Pattern regexp, Closure body) throws Throwable {
        GroovyBackend.addStepDefinition(regexp, body, locale);
    }

    public static void 但是(Pattern regexp, Closure body) throws Throwable {
        GroovyBackend.addStepDefinition(regexp, body, locale);
    }

}
