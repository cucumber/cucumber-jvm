package io.cucumber.core.options;

import cucumber.api.SnippetType;
import cucumber.runtime.CucumberException;
import cucumber.runtime.io.MultiLoader;
import cucumber.runtime.io.ResourceLoader;
import io.cucumber.core.model.Classpath;
import io.cucumber.core.model.FeaturePath;
import io.cucumber.core.model.FeatureWithLines;
import io.cucumber.core.model.GluePath;
import io.cucumber.core.model.RerunLoader;

import java.net.URI;
import java.util.regex.Pattern;

public final class CucumberOptionsAnnotationParser {
    private final RerunLoader rerunLoader;
    private boolean featuresSpecified = false;
    private boolean overridingGlueSpecified = false;
    private OptionsProvider optionsProvider;
    private CoreCucumberOptionsProvider coreCucumberOptionsProvider = new CoreCucumberOptionsProvider();

    public CucumberOptionsAnnotationParser() {
        this(new MultiLoader(CucumberOptionsAnnotationParser.class.getClassLoader()));
    }

    public CucumberOptionsAnnotationParser(ResourceLoader resourceLoader) {
        this.rerunLoader = new RerunLoader(resourceLoader);
    }

    public CucumberOptionsAnnotationParser withOptionsProvider(OptionsProvider optionsProvider){
        this.optionsProvider = optionsProvider;
        return this;
    }

    public RuntimeOptionsBuilder parse(Class<?> clazz) {
        RuntimeOptionsBuilder args = new RuntimeOptionsBuilder();

        for (Class classWithOptions = clazz; hasSuperClass(classWithOptions); classWithOptions = classWithOptions.getSuperclass()) {
            CucumberOptions options = null;
            if (optionsProvider != null) {
                options = optionsProvider.getOptions(classWithOptions);
            }
            if(options == null){
                options = coreCucumberOptionsProvider.getOptions(classWithOptions);
            }
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
                    args.setIsRerun(true);
                    URI rerunFile = FeaturePath.parse(feature.substring(1));
                    for (FeatureWithLines featureWithLines : rerunLoader.load(rerunFile)) {
                        args.addFeature(featureWithLines);
                    }
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

    private void addJunitOptions(CucumberOptions options, RuntimeOptionsBuilder args) {
        for (String junitOption : options.junit()) {
            args.addJunitOption(junitOption);
        }
    }

    private static String packagePath(Class clazz) {
        String packageName = packageName(clazz);

        if (packageName.isEmpty()) {
            return Classpath.CLASSPATH_SCHEME_PREFIX + "/";
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

        String[] junit();
    }

    private static class CoreCucumberOptions implements CucumberOptions {
        private final cucumber.api.CucumberOptions annotation;

        CoreCucumberOptions(cucumber.api.CucumberOptions annotation) {
            this.annotation = annotation;
        }

        @Override
        public boolean dryRun() {
            return annotation.dryRun();
        }

        @Override
        public boolean strict() {
            return annotation.strict();
        }

        @Override
        public String[] features() {
            return annotation.features();
        }

        @Override
        public String[] glue() {
            return annotation.glue();
        }

        @Override
        public String[] extraGlue() {
            return annotation.extraGlue();
        }

        @Override
        public String[] tags() {
            return annotation.tags();
        }

        @Override
        public String[] plugin() {
            return annotation.plugin();
        }

        @Override
        public boolean monochrome() {
            return annotation.monochrome();
        }

        @Override
        public String[] name() {
            return annotation.name();
        }

        @Override
        public SnippetType snippets() {
            return annotation.snippets();
        }

        @Override
        public String[] junit() {
            return annotation.junit();
        }
    }

    private static class CoreCucumberOptionsProvider implements OptionsProvider {
        @Override
        public CucumberOptions getOptions(Class<?> clazz) {
            final cucumber.api.CucumberOptions annotation = clazz.getAnnotation(cucumber.api.CucumberOptions.class);
            if (annotation == null) {
                return null;
            }
            return new CoreCucumberOptions(annotation);
        }
    }
}
