package io.cucumber.core.options;

import io.cucumber.core.backend.ObjectFactory;
import io.cucumber.core.exception.CucumberException;
import io.cucumber.core.feature.FeatureWithLines;
import io.cucumber.core.feature.GluePath;
import io.cucumber.core.io.Classpath;
import io.cucumber.core.snippets.SnippetType;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Pattern;

import static io.cucumber.core.options.OptionsFileParser.parseFeatureWithLinesFile;
import static java.util.Objects.requireNonNull;

public final class CucumberOptionsAnnotationParser {
    private boolean featuresSpecified = false;
    private boolean overridingGlueSpecified = false;
    private OptionsProvider optionsProvider;

    private static String packagePath(Class clazz) {
        String packageName = packageName(clazz);

        if (packageName.isEmpty()) {
            return Classpath.CLASSPATH_SCHEME_PREFIX + "/";
        }

        return Classpath.CLASSPATH_SCHEME_PREFIX + packageName.replace('.', '/');
    }

    private static String packageName(Class clazz) {
        String className = clazz.getName();
        return className.substring(0, Math.max(0, className.lastIndexOf('.')));
    }

    public CucumberOptionsAnnotationParser withOptionsProvider(OptionsProvider optionsProvider) {
        this.optionsProvider = optionsProvider;
        return this;
    }

    public RuntimeOptionsBuilder parse(Class<?> clazz) {
        RuntimeOptionsBuilder args = new RuntimeOptionsBuilder();

        for (Class classWithOptions = clazz; hasSuperClass(classWithOptions); classWithOptions = classWithOptions.getSuperclass()) {
            CucumberOptions options = requireNonNull(optionsProvider).getOptions(classWithOptions);

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
                addObjectFactory(options, args);
            }
        }
        addDefaultFeaturePathIfNoFeaturePathIsSpecified(args, clazz);
        addDefaultGlueIfNoOverridingGlueIsSpecified(args, clazz);
        return args;
    }

    private void addName(CucumberOptions options, RuntimeOptionsBuilder args) {
        for (String name : options.name()) {
            Pattern pattern = Pattern.compile(name);
            args.addNameFilter(pattern);
        }
    }

    private void addSnippets(CucumberOptions options, RuntimeOptionsBuilder args) {
        args.setSnippetType(options.snippets());
    }

    private void addDryRun(CucumberOptions options, RuntimeOptionsBuilder args) {
        if (options.dryRun()) {
            args.setDryRun(true);
        }
    }

    private void addMonochrome(CucumberOptions options, RuntimeOptionsBuilder args) {
        if (options.monochrome() || runningInEnvironmentWithoutAnsiSupport()) {
            args.setMonochrome(true);
        }
    }

    private void addTags(CucumberOptions options, RuntimeOptionsBuilder args) {
        for (String tags : options.tags()) {
            args.addTagFilter(tags);
        }
    }

    private void addPlugins(CucumberOptions options, RuntimeOptionsBuilder args) {
        for (String plugin : options.plugin()) {
            args.addPluginName(plugin, false);
        }
    }

    private void addFeatures(CucumberOptions options, RuntimeOptionsBuilder args) {
        if (options != null && options.features().length != 0) {
            for (String feature : options.features()) {
                if (feature.startsWith("@")) {
                    Path rerunFile = Paths.get(feature.substring(1));
                    args.addRerun(parseFeatureWithLinesFile(rerunFile));
                } else {
                    FeatureWithLines featureWithLines = FeatureWithLines.parse(feature);
                    args.addFeature(featureWithLines);
                }
            }
            featuresSpecified = true;
        }
    }

    private void addDefaultFeaturePathIfNoFeaturePathIsSpecified(RuntimeOptionsBuilder args, Class clazz) {
        if (!featuresSpecified) {
            String packageName = packagePath(clazz);
            FeatureWithLines featureWithLines = FeatureWithLines.parse(packageName);
            args.addFeature(featureWithLines);
        }
    }

    private void addGlue(CucumberOptions options, RuntimeOptionsBuilder args) {
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
            args.addGlue(GluePath.parse(glue));
        }
    }

    private void addDefaultGlueIfNoOverridingGlueIsSpecified(RuntimeOptionsBuilder args, Class clazz) {
        if (!overridingGlueSpecified) {
            args.addGlue(GluePath.parse(packageName(clazz)));
        }
    }

    private void addStrict(CucumberOptions options, RuntimeOptionsBuilder args) {
        if (options.strict()) {
            args.setStrict(true);
        }
    }

    private void addObjectFactory(CucumberOptions options, RuntimeOptionsBuilder args) {
        if (options.objectFactory() != null) {
            args.setObjectFactoryClass(options.objectFactory());
        }
    }

    private boolean runningInEnvironmentWithoutAnsiSupport() {
        boolean intelliJidea = System.getProperty("idea.launcher.bin.path") != null;
        // TODO: What does Eclipse use?
        return intelliJidea;
    }

    private boolean hasSuperClass(Class classWithOptions) {
        return classWithOptions != Object.class;
    }


    public interface OptionsProvider {
        CucumberOptions getOptions(Class<?> clazz);
    }

    public interface CucumberOptions {

        boolean dryRun();

        boolean strict();

        String[] features();

        String[] glue();

        String[] extraGlue();

        String[] tags();

        String[] plugin();

        boolean monochrome();

        String[] name();

        SnippetType snippets();

        Class<? extends ObjectFactory> objectFactory();
    }
}
