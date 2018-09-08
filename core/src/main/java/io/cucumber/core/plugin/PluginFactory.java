package io.cucumber.core.plugin;

import cucumber.api.Plugin;
import io.cucumber.core.exception.CucumberException;
import io.cucumber.core.options.PluginOption;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import static java.util.Arrays.asList;

/**
 * This class creates plugin instances from a String.
 * <p>
 * The String is of the form name[:output] where name is either a fully qualified class name or one of the built-in
 * short names. The output is optional for some plugins (and mandatory for some).
 * </ul>
 *
 * @see Plugin for specific requirements
 */
public final class PluginFactory {
    private final Class[] CTOR_PARAMETERS = new Class[]{String.class, Appendable.class, URI.class, URL.class, File.class};

    private String defaultOutFormatter = null;

    private Appendable defaultOut = new PrintStream(System.out) {
        @Override
        public void close() {
            // We have no intention to close System.out
        }
    };

    static URL toURL(String pathOrUrl) {
        try {
            if (!pathOrUrl.endsWith("/")) {
                pathOrUrl = pathOrUrl + "/";
            }
            if (pathOrUrl.matches("^(file|http|https):.*")) {
                return new URL(pathOrUrl);
            } else {
                return new URL("file:" + pathOrUrl);
            }
        } catch (MalformedURLException e) {
            throw new CucumberException("Bad URL:" + pathOrUrl, e);
        }
    }

    public Plugin create(Options.Plugin plugin) {
        try {
            return instantiate(plugin.pluginString(), plugin.pluginClass(), plugin.argument());
        } catch (IOException | URISyntaxException e) {
            throw new CucumberException(e);
        }
    }

    private <T extends Plugin> T instantiate(String pluginString, Class<T> pluginClass, String argument) throws IOException, URISyntaxException {
        Constructor<T> single = findSingleArgConstructor(pluginClass);
        Constructor<T> empty = findEmptyConstructor(pluginClass);

        if (single != null) {
            Object ctorArg = convertOrNull(argument, single.getParameterTypes()[0], pluginString);
            if (ctorArg != null)
                return newInstance(single, ctorArg);
        }
        if (argument == null && empty != null) {
            return newInstance(empty);
        }
        if (single != null)
            throw new CucumberException(String.format("You must supply an output argument to %s. Like so: %s:output", pluginString, pluginString));

        throw new CucumberException(String.format("%s must have a constructor that is either empty or a single arg of one of: %s", pluginClass, asList(CTOR_PARAMETERS)));
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

    private Object convertOrNull(String arg, Class ctorArgClass, String formatterString) throws IOException, URISyntaxException {
        if (arg == null) {
            if (ctorArgClass.equals(Appendable.class)) {
                return defaultOutOrFailIfAlreadyUsed(formatterString);
            } else {
                return null;
            }
        }
        if (ctorArgClass.equals(URI.class)) {
            return new URI(arg);
        }
        if (ctorArgClass.equals(URL.class)) {
            return toURL(arg);
        }
        if (ctorArgClass.equals(File.class)) {
            return new File(arg);
        }
        if (ctorArgClass.equals(String.class)) {
            return arg;
        }
        if (ctorArgClass.equals(Appendable.class)) {
            return new UTF8OutputStreamWriter(new URLOutputStream(toURL(arg)));
        }
        return null;
    }

    private <T> Constructor<T> findSingleArgConstructor(Class<T> pluginClass) {
        Constructor<T> constructor = null;
        for (Class ctorArgClass : CTOR_PARAMETERS) {
            try {
                Constructor<T> candidate = pluginClass.getConstructor(ctorArgClass);
                if (constructor != null) {
                    throw new CucumberException(String.format("Plugin %s should only define a single one-argument constructor", pluginClass.getName()));
                }
                constructor = candidate;
            } catch (NoSuchMethodException ignore) {
            }
        }
        return constructor;
    }

    private <T> Constructor<T> findEmptyConstructor(Class<T> pluginClass) {
        try {
            return pluginClass.getConstructor();
        } catch (NoSuchMethodException ignore) {
            return null;
        }
    }

    private Appendable defaultOutOrFailIfAlreadyUsed(String formatterString) {
        try {
            if (defaultOut != null) {
                defaultOutFormatter = formatterString;
                return defaultOut;
            } else {
                throw new CucumberException("Only one plugin can use STDOUT, now both " +
                    defaultOutFormatter + " and " + formatterString + " use it. " +
                    "If you use more than one plugin you must specify output path with PLUGIN:PATH_OR_URL");
            }
        } finally {
            defaultOut = null;
        }
    }

}
