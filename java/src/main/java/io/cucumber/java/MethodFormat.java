package io.cucumber.java;

import io.cucumber.core.backend.CucumberBackendException;

import java.lang.reflect.Method;
import java.security.ProtectionDomain;
import java.text.MessageFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Helper class for formatting a method signature to a shorter form.
 */
final class MethodFormat {
    static final MethodFormat FULL = new MethodFormat("%qc.%m(%a) in %s");
    private static final Pattern METHOD_PATTERN = Pattern.compile("((?:static\\s|public\\s)+)([^\\s]*)\\s\\.?(.*)\\.([^\\(]*)\\(([^\\)]*)\\)(?: throws )?(.*)");
    private static final Pattern PACKAGE_PATTERN = Pattern.compile("[^,<>]*\\.");
    private final MessageFormat format;

    /**
     * @param format the format string to use. There are several pattern tokens that can be used:
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
            .replaceAll("%M", "{0}")
            .replaceAll("%r", "{1}")
            .replaceAll("%qc", "{2}")
            .replaceAll("%m", "{3}")
            .replaceAll("%qa", "{4}")
            .replaceAll("%qe", "{5}")
            .replaceAll("%c", "{6}")
            .replaceAll("%a", "{7}")
            .replaceAll("%e", "{8}")
            .replaceAll("%s", "{9}");
        this.format = new MessageFormat(pattern);
    }

    private static String removePackage(String qc) {
        return PACKAGE_PATTERN.matcher(qc).replaceAll("");
    }

    String format(Method method) {
        String signature = method.toGenericString();
        Matcher matcher = METHOD_PATTERN.matcher(signature);
        if (matcher.find()) {
            String M = matcher.group(1);
            String r = matcher.group(2);
            String qc = matcher.group(3);
            String m = matcher.group(4);
            String qa = matcher.group(5);
            String qe = matcher.group(6);
            String c = removePackage(qc);
            String a = removePackage(qa);
            String e = removePackage(qe);
            String s = getCodeSource(method);

            return format.format(new Object[]{
                M,
                r,
                qc,
                m,
                qa,
                qe,
                c,
                a,
                e,
                s
            });
        } else {
            throw new CucumberBackendException("Cucumber bug: Couldn't format " + signature);
        }
    }

    private String getCodeSource(Method method) {
        try {
            ProtectionDomain protectionDomain = method.getDeclaringClass().getProtectionDomain();
            return protectionDomain.getCodeSource().getLocation().toExternalForm();
        } catch (Exception e) {
            // getProtectionDomain() returns null on some platforms (for example on Android)
            return method.getDeclaringClass().getName();
        }
    }
}
