package io.cucumber.core.options;

import io.cucumber.core.backend.ObjectFactory;
import io.cucumber.core.exception.CucumberException;
import io.cucumber.core.feature.FeatureWithLines;
import io.cucumber.core.feature.GluePath;
import io.cucumber.core.snippets.SnippetType;
import io.cucumber.tagexpressions.TagExpressionException;
import io.cucumber.tagexpressions.TagExpressionParser;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Pattern;

import static io.cucumber.core.options.OptionsFileParser.parseFeatureWithLinesFile;
import static io.cucumber.core.resource.ClasspathSupport.CLASSPATH_SCHEME_PREFIX;
import static java.util.Objects.requireNonNull;

public final class CucumberOptionsAnnotationParser {

    private boolean featuresSpecified = false;
    private boolean overridingGlueSpecified = false;
    private OptionsProvider optionsProvider;

    public CucumberOptionsAnnotationParser withOptionsProvider(OptionsProvider optionsProvider) {
        this.optionsProvider = optionsProvider;
        return this;
    }

    public RuntimeOptionsBuilder parse(Class<?> clazz) {
        RuntimeOptionsBuilder args = new RuntimeOptionsBuilder();

        for (Class<?> classWithOptions = clazz; hasSuperClass(
            classWithOptions); classWithOptions = classWithOptions.getSuperclass()) {
            CucumberOptions options = requireNonNull(optionsProvider).getOptions(classWithOptions);

            if (options != null) {
                addDryRun(options, args);
                addMonochrome(options, args);
                addTags(classWithOptions, options, args);
                addPlugins(options, args);
                addPublish(options, args);
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

    private boolean hasSuperClass(Class<?> classWithOptions) {
        return classWithOptions != Object.class;
    }

    private void addDryRun(CucumberOptions options, RuntimeOptionsBuilder args) {
        if (options.dryRun()) {
            args.setDryRun(true);
        }
    }

    private void addMonochrome(CucumberOptions options, RuntimeOptionsBuilder args) {
        if (options.monochrome()) {
            args.setMonochrome(true);
        }
    }

    private void addTags(Class<?> clazz, CucumberOptions options, RuntimeOptionsBuilder args) {
        String tagExpression = options.tags();
        if (!tagExpression.isEmpty()) {
            try {
                args.addTagFilter(TagExpressionParser.parse(tagExpression));
            } catch (TagExpressionException tee) {
                throw new IllegalArgumentException(String.format("Invalid tag expression at '%s'", clazz.getName()),
                    tee);
            }
        }
    }

    private void addPlugins(CucumberOptions options, RuntimeOptionsBuilder args) {
        for (String plugin : options.plugin()) {
            args.addPluginName(plugin);
        }
    }

    private void addPublish(CucumberOptions options, RuntimeOptionsBuilder args) {
        if (options.publish()) {
            args.setPublish(true);
        }
    }

    private void addStrict(CucumberOptions options, RuntimeOptionsBuilder args) {
        if (!options.strict()) {
            throw new CucumberException(
                "@CucumberOptions(strict=false) is no longer supported. Please use strict=true");
        }
    }

    private void addName(CucumberOptions options, RuntimeOptionsBuilder args) {
        for (String name : options.name()) {
            Pattern pattern = Pattern.compile(name);
            args.addNameFilter(pattern);
        }
    }

    private void addSnippets(CucumberOptions options, RuntimeOptionsBuilder args) {
        if (options.snippets() != SnippetType.UNDERSCORE) {
            args.setSnippetType(options.snippets());
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

    private void addObjectFactory(CucumberOptions options, RuntimeOptionsBuilder args) {
        if (options.objectFactory() != null) {
            args.setObjectFactoryClass(options.objectFactory());
        }
    }

    private void addDefaultFeaturePathIfNoFeaturePathIsSpecified(RuntimeOptionsBuilder args, Class<?> clazz) {
        if (!featuresSpecified) {
            String packageName = packagePath(clazz);
            FeatureWithLines featureWithLines = FeatureWithLines.parse(packageName);
            args.addFeature(featureWithLines);
        }
    }

    private void addDefaultGlueIfNoOverridingGlueIsSpecified(RuntimeOptionsBuilder args, Class<?> clazz) {
        if (!overridingGlueSpecified) {
            args.addGlue(GluePath.parse(packageName(clazz)));
        }
    }

    private static String packagePath(Class<?> clazz) {
        String packageName = packageName(clazz);

        if (packageName.isEmpty()) {
            return CLASSPATH_SCHEME_PREFIX + "/";
        }

        return CLASSPATH_SCHEME_PREFIX + "/" + packageName.replace('.', '/');
    }

    private static String packageName(Class<?> clazz) {
        String className = clazz.getName();
        return className.substring(0, Math.max(0, className.lastIndexOf('.')));
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

        String tags();

        String[] plugin();

        boolean publish();

        boolean monochrome();

        String[] name();

        SnippetType snippets();

        Class<? extends ObjectFactory> objectFactory();

    }

}
