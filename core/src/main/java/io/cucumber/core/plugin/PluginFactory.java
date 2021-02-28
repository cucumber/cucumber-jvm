package io.cucumber.core.plugin;

import io.cucumber.core.exception.CucumberException;
import io.cucumber.core.logging.Logger;
import io.cucumber.core.logging.LoggerFactory;
import io.cucumber.core.options.CurlOption;
import io.cucumber.plugin.Plugin;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;

/**
 * This class creates plugin instances from a String.
 * <p>
 * The String is of the form name[:output] where name is either a fully
 * qualified class name or one of the built-in short names. The output is
 * optional for some plugins (and mandatory for some).
 *
 * @see Plugin for specific requirements
 */
public final class PluginFactory {

    private static final Logger log = LoggerFactory.getLogger(PluginFactory.class);

    private final Class<?>[] CTOR_PARAMETERS = new Class<?>[] {
            String.class,
            File.class,
            URI.class,
            URL.class,
            OutputStream.class,
            // Deprecated
            Appendable.class
    };

    private String pluginUsingDefaultOut = null;

    private PrintStream defaultOut = new PrintStream(System.out) {
        @Override
        public void close() {
            // We have no intention to close System.out
        }
    };

    Plugin create(Options.Plugin plugin) {
        try {
            return instantiate(plugin.pluginString(), plugin.pluginClass(), plugin.argument());
        } catch (IOException | URISyntaxException e) {
            throw new CucumberException(e);
        }
    }

    private <T extends Plugin> T instantiate(String pluginString, Class<T> pluginClass, String argument)
            throws IOException, URISyntaxException {
        Map<Class<?>, Constructor<T>> singleArgConstructors = findSingleArgConstructors(pluginClass);
        if (argument == null) {// No argument passed
            Constructor<T> outputStreamConstructor = singleArgConstructors.get(OutputStream.class);
            if (outputStreamConstructor != null) {
                return newInstance(outputStreamConstructor, defaultOutOrFailIfAlreadyUsed(pluginString));
            }
            Constructor<T> emptyConstructor = findEmptyConstructor(pluginClass);
            if (emptyConstructor != null) {
                return newInstance(emptyConstructor);
            }
            if (!singleArgConstructors.isEmpty()) {
                throw new CucumberException(String.format(
                    "You must supply an output argument to %s. Like so: %s:DIR|FILE|URL", pluginString,
                    pluginString));
            }
            throw new CucumberException(String.format(
                "%s must have at least one empty constructor or a constructor that declares a single parameter of one of: %s",
                pluginClass, asList(CTOR_PARAMETERS)));
        }
        if (singleArgConstructors.size() != 1) {
            throw new CucumberException(
                String.format("%s must have exactly one constructor that declares a single parameter of one of: %s",
                    pluginClass, asList(CTOR_PARAMETERS)));
        }
        Map.Entry<Class<?>, Constructor<T>> singleArgConstructorEntry = singleArgConstructors.entrySet().iterator()
                .next();
        Class<?> parameterType = singleArgConstructorEntry.getKey();
        Constructor<T> singleArgConstructor = singleArgConstructorEntry.getValue();
        return newInstance(singleArgConstructor, convert(argument, parameterType, pluginString, pluginClass));
    }

    private <T> Map<Class<?>, Constructor<T>> findSingleArgConstructors(Class<T> pluginClass) {
        Map<Class<?>, Constructor<T>> result = new HashMap<>();

        for (Class<?> ctorArgClass : CTOR_PARAMETERS) {
            try {
                result.put(ctorArgClass, pluginClass.getConstructor(ctorArgClass));
            } catch (NoSuchMethodException ignore) {
            }
        }
        return result;
    }

    private <T extends Plugin> T newInstance(Constructor<T> constructor, Object... ctorArgs) {
        try {
            return constructor.newInstance(ctorArgs);
        } catch (InstantiationException | IllegalAccessException e) {
            throw new CucumberException(e);
        } catch (InvocationTargetException e) {
            throw new CucumberException(e.getTargetException());
        }
    }

    private PrintStream defaultOutOrFailIfAlreadyUsed(String pluginString) {
        try {
            if (defaultOut != null) {
                pluginUsingDefaultOut = pluginString;
                return defaultOut;
            } else {
                throw new CucumberException("Only one plugin can use STDOUT, now both " +
                        pluginUsingDefaultOut + " and " + pluginString + " use it. " +
                        "If you use more than one plugin you must specify output path with " + pluginString
                        + ":DIR|FILE|URL");
            }
        } finally {
            defaultOut = null;
        }
    }

    private <T extends Plugin> Constructor<T> findEmptyConstructor(Class<T> pluginClass) {
        try {
            return pluginClass.getConstructor();
        } catch (NoSuchMethodException ignore) {
            return null;
        }
    }

    private Object convert(String arg, Class<?> ctorArgClass, String pluginString, Class<?> pluginClass)
            throws IOException, URISyntaxException {
        if (ctorArgClass.equals(URI.class)) {
            return makeURL(arg).toURI();
        }
        if (ctorArgClass.equals(URL.class)) {
            return makeURL(arg);
        }
        if (ctorArgClass.equals(File.class)) {
            return new File(arg);
        }
        if (ctorArgClass.equals(String.class)) {
            return arg;
        }
        if (ctorArgClass.equals(OutputStream.class)) {
            if (arg == null) {
                return defaultOutOrFailIfAlreadyUsed(pluginString);
            } else {
                return openStream(arg);
            }
        }

        if (ctorArgClass.equals(Appendable.class)) {
            String recommendedParameters = Arrays.stream(CTOR_PARAMETERS)
                    .filter(c -> c != Appendable.class)
                    .map(Class::getName)
                    .collect(Collectors.joining(", "));
            log.error(() -> String.format(
                "The %s plugin class takes a java.lang.Appendable in its constructor, which is deprecated and will be removed in the next major release. It should be changed to accept one of %s",
                pluginClass.getName(), recommendedParameters));
            return new UTF8OutputStreamWriter(openStream(arg));
        }
        throw new CucumberException(
            String.format("Cannot convert %s into a %s to pass to the %s plugin", arg, ctorArgClass, pluginString));
    }

    private static URL makeURL(String arg) throws MalformedURLException {
        if (arg.matches("^(file|http|https):.*")) {
            return new URL(arg);
        } else {
            return new URL("file:" + arg);
        }
    }

    private static OutputStream openStream(String arg) throws IOException {
        if (arg.matches("^(http|https):.*")) {
            CurlOption option = CurlOption.parse(arg);
            return new UrlOutputStream(option, null);
        } else if (arg.matches("^file:.*")) {
            return createFileOutputStream(new File(new URL(arg).getFile()));
        } else {
            return createFileOutputStream(new File(arg));
        }
    }

    private static FileOutputStream createFileOutputStream(File file) {
        File canonicalFile;
        try {
            canonicalFile = file.getCanonicalFile();
        } catch (IOException e) {
            throw new IllegalArgumentException(String.format("" +
                    "Couldn't get the canonical file of '%s'.\n" +
                    "The details are in the stack trace below:",
                file),
                e);
        }

        try {
            File parentFile = canonicalFile.getParentFile();
            if (parentFile != null) {
                Files.createDirectories(parentFile.toPath());
            }
        } catch (IOException e) {
            // See: https://github.com/cucumber/cucumber-jvm/issues/2108
            throw new IllegalArgumentException(String.format("" +
                    "Couldn't create parent directories of '%s'.\n" +
                    "Make sure the the parent directory '%s' isn't a file.\n" +
                    "\n" +
                    "Note: This usually happens when plugins write to colliding paths.\n" +
                    "For example: 'html:target/cucumber, json:target/cucumber/report.json'\n" +
                    "You can fix this by making the paths do no collide.\n" +
                    "For example: 'html:target/cucumber/report.html, json:target/cucumber/report.json'" +
                    "\n" +
                    "The details are in the stack trace below:",
                canonicalFile, canonicalFile.getParentFile()),
                e);
        }

        try {
            return new FileOutputStream(file);
        } catch (FileNotFoundException e) {
            // See: https://github.com/cucumber/cucumber-jvm/issues/2108
            throw new IllegalArgumentException(String.format("" +
                    "Couldn't create a file output stream for '%s'.\n" +
                    "Make sure the the file isn't a directory.\n" +
                    "\n" +
                    "Note: This usually happens when plugins write to colliding paths.\n" +
                    "For example: 'json:target/cucumber/report.json, html:target/cucumber'\n" +
                    "You can fix this by making the paths do no collide.\n" +
                    "For example: 'json:target/cucumber/report.json, html:target/cucumber/report.html'" +
                    "\n" +
                    "The details are in the stack trace below:",
                file),
                e);
        }
    }

}
