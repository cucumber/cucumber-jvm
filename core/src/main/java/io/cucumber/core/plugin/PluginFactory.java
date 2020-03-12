package io.cucumber.core.plugin;

import io.cucumber.core.exception.CucumberException;
import io.cucumber.core.logging.Logger;
import io.cucumber.core.logging.LoggerFactory;
import io.cucumber.plugin.Plugin;

import java.io.File;
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
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;

/**
 * This class creates plugin instances from a String.
 * <p>
 * The String is of the form name[:output] where name is either a fully qualified class name or one of the built-in
 * short names. The output is optional for some plugins (and mandatory for some).
 *
 * @see Plugin for specific requirements
 */
public final class PluginFactory {
    private static final Logger log = LoggerFactory.getLogger(PluginFactory.class);

    private final Class<?>[] CTOR_PARAMETERS = new Class<?>[]{
        String.class,
        File.class,
        URI.class,
        URL.class,
        OutputStream.class,
        // Deprecated
        Appendable.class
    };

    private String formatterUsingDefaultOut = null;

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

    private <T extends Plugin> T instantiate(String pluginString, Class<T> pluginClass, String argument) throws IOException, URISyntaxException {
        Map<Class<?>, Constructor<T>> singleArgConstructors = findSingleArgConstructors(pluginClass);

        if (singleArgConstructors.isEmpty()) {
            Constructor<T> emptyConstructor = findEmptyConstructor(pluginClass);
            if (emptyConstructor == null) {
                throw new CucumberException(String.format("%s must have at least one empty constructor or a constructor that declares a single parameter of one of: %s", pluginClass, asList(CTOR_PARAMETERS)));
            } else if (argument != null) {
                throw new CucumberException(String.format("Cannot pass argument %s to empty constructor for %s", argument, pluginClass));
            } else {
                return newInstance(emptyConstructor);
            }
        }

        if (argument == null) {
            if (formatterUsingDefaultOut != null) {
                throw new CucumberException(String.format("Only one plugin can use STDOUT, now both %s and %s use it. " +
                    "If you use more than one plugin you must specify output path with %s:DIR|FILE|URL", formatterUsingDefaultOut, pluginString, pluginString));
            }
            Constructor<T> printStreamConstructor = singleArgConstructors.get(PrintStream.class);
            if (printStreamConstructor != null) {
                Object ctorArg = convert(argument, PrintStream.class, pluginString, pluginClass);
                return newInstance(printStreamConstructor, ctorArg);
            } else {
                throw new CucumberException(String.format("You must supply an output argument to %s. Like so: %s:DIR|FILE|URL", pluginString, pluginString));
            }
        }

        for (Map.Entry<Class<?>, Constructor<T>> constructorEntry : singleArgConstructors.entrySet()) {
            Class<?> ctorArgType = constructorEntry.getKey();
            if (!PrintStream.class.equals(ctorArgType)) {
                Object ctorArg = convert(argument, ctorArgType, pluginString, pluginClass);
                Constructor<T> ctor = constructorEntry.getValue();
                return newInstance(ctor, ctorArg);
            }
        }

        throw new CucumberException(String.format("Sorry there was nothing I could do%s", "!"));
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

    private Object convert(String arg, Class<?> ctorArgClass, String pluginString, Class<?> pluginClass) throws IOException, URISyntaxException {
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
                return openStream(makeURL(arg));
            }
        }

        if (ctorArgClass.equals(Appendable.class)) {
            String recommendedParameters = Arrays.stream(CTOR_PARAMETERS)
                .filter(c -> c != Appendable.class)
                .map(Class::getName)
                .collect(Collectors.joining(", "));
            log.error(() -> String.format("The %s plugin class takes a java.lang.Appendable in its constructor, which is deprecated and will be removed in the next major release. It should be changed to accept one of %s", pluginClass.getName(), recommendedParameters));
            return openStream(makeURL(arg));
        }
        throw new CucumberException(String.format("Cannot convert %s into a %s to pass to the %s plugin", arg, ctorArgClass, pluginString));
    }

    private static URL makeURL(String arg) throws MalformedURLException {
        if (arg.matches("^(file|http|https):.*")) {
            return new URL(arg);
        } else {
            return new URL("file:" + arg);
        }
    }

    private static OutputStream openStream(URL url) throws IOException {
        return url.getProtocol().equals("file") ? new FileOutputStream(url.getFile()) :
            new URLOutputStream(url);
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

    private <T extends Plugin> Constructor<T> findEmptyConstructor(Class<T> pluginClass) {
        try {
            return pluginClass.getConstructor();
        } catch (NoSuchMethodException ignore) {
            return null;
        }
    }

    private PrintStream defaultOutOrFailIfAlreadyUsed(String formatterString) {
        try {
            if (defaultOut != null) {
                formatterUsingDefaultOut = formatterString;
                return defaultOut;
            } else {
                throw new CucumberException("Only one plugin can use STDOUT, now both " +
                    formatterUsingDefaultOut + " and " + formatterString + " use it. " +
                    "If you use more than one plugin you must specify output path with PLUGIN:PATH_OR_URL");
            }
        } finally {
            defaultOut = null;
        }
    }
}
