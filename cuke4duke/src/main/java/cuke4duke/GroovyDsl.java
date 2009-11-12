package cuke4duke;

import cuke4duke.internal.groovy.GroovyHook;
import cuke4duke.internal.groovy.GroovyLanguage;
import cuke4duke.internal.groovy.GroovyStepDefinition;
import cuke4duke.internal.language.LanguageMixin;
import groovy.lang.Closure;

import java.util.regex.Pattern;

/**
 * The DSL for Groovy step definitions.
 */
public class GroovyDsl {
    public static GroovyLanguage groovyLanguage;
    public static LanguageMixin languageMixin;

    public static void World(Closure body) {
        groovyLanguage.registerWorldFactory(body);
    }

    public static void Before(Object... tagsAndBody) {
        String[] tagNames = new String[tagsAndBody.length-1];
        System.arraycopy(tagsAndBody, 0, tagNames, 0, tagNames.length);
        Closure body = (Closure) tagsAndBody[tagsAndBody.length-1];
        languageMixin.add_hook("before", new GroovyHook(tagNames, body, groovyLanguage));
    }

    public static void After(Object... tagsAndBody) {
        String[] tagNames = new String[tagsAndBody.length-1];
        System.arraycopy(tagsAndBody, 0, tagNames, 0, tagNames.length);
        Closure body = (Closure) tagsAndBody[tagsAndBody.length-1];
        languageMixin.add_hook("after", new GroovyHook(tagNames, body, groovyLanguage));
    }

    public static void Given(Pattern regexp, Closure body) throws Throwable {
        registerStepDefinition(regexp, body);
    }

    public static void When(Pattern regexp, Closure body) throws Throwable {
        registerStepDefinition(regexp, body);
    }

    public static void Then(Pattern regexp, Closure body) throws Throwable {
        registerStepDefinition(regexp, body);
    }

    private static void registerStepDefinition(Pattern regexp, Closure body) throws Throwable {
        groovyLanguage.addStepDefinition(new GroovyStepDefinition(groovyLanguage, regexp, body));
    }
}
