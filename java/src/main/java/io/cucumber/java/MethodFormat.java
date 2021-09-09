package io.cucumber.java;

import io.cucumber.core.backend.CucumberBackendException;

import java.lang.reflect.Method;
import java.text.MessageFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Helper class for formatting a method signature to a shorter form.
 */
final class MethodFormat {

    static final MethodFormat FULL = new MethodFormat("%qc.%m(%qa)");
    private static final Pattern METHOD_PATTERN = Pattern
            .compile("((?:static\\s|public\\s)+)([^\\s]*)\\s\\.?(.*)\\.([^\\(]*)\\(([^\\)]*)\\)(?: throws )?(.*)");
    private final MessageFormat format;

    /**
     * @param format the format string to use. There are several pattern tokens
     *               that can be used:
     *               <ul>
     *               <li><strong>%M</strong>: Modifiers</li>
     *               <li><strong>%qr</strong>: Qualified return type</li>
     *               <li><strong>%r</strong>: Unqualified return type</li>
     *               <li><strong>%qc</strong>: Qualified class</li>
     *               <li><strong>%c</strong>: Unqualified class</li>
     *               <li><strong>%m</strong>: Method name</li>
     *               <li><strong>%qa</strong>: Qualified arguments</li>
     *               <li><strong>%a</strong>: Unqualified arguments</li>
     *               <li><strong>%qe</strong>: Qualified exceptions</li>
     *               <li><strong>%e</strong>: Unqualified exceptions</li>
     *               <li><strong>%s</strong>: Code source</li>
     *               </ul>
     */
    private MethodFormat(String format) {
        String pattern = format
                .replaceAll("%qc", "{0}")
                .replaceAll("%m", "{1}")
                .replaceAll("%qa", "{2}");
        this.format = new MessageFormat(pattern);
    }

    String format(Method method) {
        String signature = method.toGenericString();
        Matcher matcher = METHOD_PATTERN.matcher(signature);
        if (matcher.find()) {
            String qc = matcher.group(3);
            String m = matcher.group(4);
            String qa = matcher.group(5);

            return format.format(new Object[] {
                    qc,
                    m,
                    qa,
            });
        } else {
            throw new CucumberBackendException("Cucumber bug: Couldn't format " + signature);
        }
    }

}
