package io.cucumber.core.options;

import static java.util.Arrays.asList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.cucumber.core.api.options.CucumberOptions;
import io.cucumber.core.exception.CucumberException;
import io.cucumber.core.io.MultiLoader;
import io.cucumber.core.io.ResourceLoader;
import io.cucumber.core.model.Classpath;

public final class RuntimeOptionsFactory {
    private final Class clazz;
    private final ResourceLoader resoureceLoader;
    private boolean featuresSpecified = false;
    private boolean overridingGlueSpecified = false;

    public RuntimeOptionsFactory(Class clazz) {
        this(clazz, new MultiLoader(clazz.getClassLoader()));
    }

    public RuntimeOptionsFactory(Class clazz, ResourceLoader resourceLoader) {
        this.clazz = clazz;
        this.resoureceLoader = resourceLoader;
    }

    public RuntimeOptions create() {
        List<String> args = buildArgsFromOptions();
        return new RuntimeOptions(resoureceLoader, args);
    }

    private List<String> buildArgsFromOptions() {
        List<String> args = new ArrayList<String>();

        for (Class classWithOptions = clazz; hasSuperClass(classWithOptions); classWithOptions = classWithOptions.getSuperclass()) {
            CucumberOptions options = getOptions(classWithOptions);
            if (options != null) {
                addDryRun(options, args);
                addMonochrome(options, args);
                addTags(options, args);
                addPlugins(options, args);
                addStrict(options, args);
                addName(options, args);
                addSnippets(options, args);
                addGlue(options, args);
                addFeatures(options, args);
                addJunitOptions(options, args);
            }
        }
        addDefaultFeaturePathIfNoFeaturePathIsSpecified(args, clazz);
        addDefaultGlueIfNoOverridingGlueIsSpecified(args, clazz);
        return args;
    }

    private void addName(CucumberOptions options, List<String> args) {
        for (String name : options.name()) {
            args.add("--name");
            args.add(name);
        }
    }

    private void addSnippets(CucumberOptions options, List<String> args) {
        args.add("--snippets");
        args.add(options.snippets().toString());
    }

    private void addDryRun(CucumberOptions options, List<String> args) {
        if (options.dryRun()) {
            args.add("--dry-run");
        }
    }

    private void addMonochrome(CucumberOptions options, List<String> args) {
        if (options.monochrome() || runningInEnvironmentWithoutAnsiSupport()) {
            args.add("--monochrome");
        }
    }

    private void addTags(CucumberOptions options, List<String> args) {
        for (String tags : options.tags()) {
            args.add("--tags");
            args.add(tags);
        }
    }

    private void addPlugins(CucumberOptions options, List<String> args) {
        List<String> plugins = new ArrayList<>();
        plugins.addAll(asList(options.plugin()));
        for (String plugin : plugins) {
            args.add("--plugin");
            args.add(plugin);
        }
    }

    private void addFeatures(CucumberOptions options, List<String> args) {
        if (options != null && options.features().length != 0) {
            Collections.addAll(args, options.features());
            featuresSpecified = true;
        }
    }

    private void addDefaultFeaturePathIfNoFeaturePathIsSpecified(List<String> args, Class clazz) {
        if (!featuresSpecified) {
            args.add(packagePath(clazz));
        }
    }

    private void addGlue(CucumberOptions options, List<String> args) {
        boolean hasExtraGlue = options.extraGlue().length > 0;
        boolean hasGlue = options.glue().length > 0;

        if (hasExtraGlue && hasGlue) {
            throw new CucumberException("glue and extraGlue cannot be specified at the same time");
        }

        String[] gluePaths = {};
        if (hasExtraGlue) {
            gluePaths = options.extraGlue();
        }
        if (hasGlue) {
            gluePaths = options.glue();
            overridingGlueSpecified = true;
        }

        for (String glue : gluePaths) {
            args.add("--glue");
            args.add(glue);
        }
    }

    private void addDefaultGlueIfNoOverridingGlueIsSpecified(List<String> args, Class clazz) {
        if (!overridingGlueSpecified) {
            args.add("--glue");
            args.add(packageName(clazz));
        }
    }


    private void addStrict(CucumberOptions options, List<String> args) {
        if (options.strict()) {
            args.add("--strict");
        }
    }

    private void addJunitOptions(CucumberOptions options, List<String> args) {
        for (String junitOption : options.junit()) {
            args.add("--junit," + junitOption);
        }
    }

    private static String packagePath(Class clazz) {
        String packageName = packageName(clazz);

        if (packageName.isEmpty()) {
            return Classpath.CLASSPATH_SCHEME_PREFIX +  "/";
        }

        return Classpath.CLASSPATH_SCHEME_PREFIX + packageName.replace('.', '/');
    }

    static String packageName(Class clazz) {
        String className = clazz.getName();
        return className.substring(0, Math.max(0, className.lastIndexOf('.')));
    }

    private boolean runningInEnvironmentWithoutAnsiSupport() {
        boolean intelliJidea = System.getProperty("idea.launcher.bin.path") != null;
        // TODO: What does Eclipse use?
        return intelliJidea;
    }

    private boolean hasSuperClass(Class classWithOptions) {
        return classWithOptions != Object.class;
    }

    private CucumberOptions getOptions(Class<?> clazz) {
        return clazz.getAnnotation(CucumberOptions.class);
    }
}
