package cucumber.runtime;

import cucumber.api.CucumberOptions;
import io.cucumber.core.model.Classpath;
import io.cucumber.core.model.FeatureWithLines;
import io.cucumber.core.model.GluePath;

import java.util.regex.Pattern;

public class RuntimeOptionsFactory {
    private final Class clazz;
    private boolean featuresSpecified = false;
    private boolean overridingGlueSpecified = false;

    public RuntimeOptionsFactory(Class clazz) {
        this.clazz = clazz;
    }

    public RuntimeOptions create() {
        RuntimeOptions runtimeOptions = RuntimeOptions.defaultOptions();
        buildArgsFromOptions().apply(runtimeOptions);

        new EnvironmentOptionsParser()
            .parse(Env.INSTANCE)
            .apply(runtimeOptions);

        runtimeOptions.addDefaultFormatterIfNotPresent();
        runtimeOptions.addDefaultSummaryPrinterIfNotPresent();

        return runtimeOptions;
    }

    private RuntimeOptionsParser.ParsedOptions buildArgsFromOptions() {
        RuntimeOptionsParser.ParsedOptions args = new RuntimeOptionsParser.ParsedOptions();

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

    private void addName(CucumberOptions options, RuntimeOptionsParser.ParsedOptions args) {
        for (String name : options.name()) {
            Pattern pattern = Pattern.compile(name);
            args.addNameFilter(pattern);
        }
    }

    private void addSnippets(CucumberOptions options, RuntimeOptionsParser.ParsedOptions args) {
        args.setSnippetType(options.snippets());
    }

    private void addDryRun(CucumberOptions options, RuntimeOptionsParser.ParsedOptions args) {
        if (options.dryRun()) {
            args.setDryRun(true);
        }
    }

    private void addMonochrome(CucumberOptions options, RuntimeOptionsParser.ParsedOptions args) {
        if (options.monochrome() || runningInEnvironmentWithoutAnsiSupport()) {
            args.setMonochrome(true);
        }
    }

    private void addTags(CucumberOptions options, RuntimeOptionsParser.ParsedOptions args) {
        for (String tags : options.tags()) {
            args.addTagFilter(tags);
        }
    }

    private void addPlugins(CucumberOptions options, RuntimeOptionsParser.ParsedOptions args) {
        for (String plugin : options.plugin()) {
            args.addPluginName(plugin, false);
        }
    }

    private void addFeatures(CucumberOptions options, RuntimeOptionsParser.ParsedOptions args) {
        if (options != null && options.features().length != 0) {
            for (String feature : options.features()) {
                FeatureWithLines featureWithLines = FeatureWithLines.parse(feature);
                args.addFeature(featureWithLines);
            }
            featuresSpecified = true;
        }
    }

    private void addDefaultFeaturePathIfNoFeaturePathIsSpecified(RuntimeOptionsParser.ParsedOptions args, Class clazz) {
        if (!featuresSpecified) {
            String packageName = packagePath(clazz);
            FeatureWithLines featureWithLines = FeatureWithLines.parse(packageName);
            args.addFeature(featureWithLines);
        }
    }

    private void addGlue(CucumberOptions options, RuntimeOptionsParser.ParsedOptions args) {
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

    private void addDefaultGlueIfNoOverridingGlueIsSpecified(RuntimeOptionsParser.ParsedOptions args, Class clazz) {
        if (!overridingGlueSpecified) {
            args.addGlue(GluePath.parse(packageName(clazz)));
        }
    }


    private void addStrict(CucumberOptions options, RuntimeOptionsParser.ParsedOptions args) {
        if (options.strict()) {
            args.setStrict(true);
        }
    }

    private void addJunitOptions(CucumberOptions options, RuntimeOptionsParser.ParsedOptions args) {
        for (String junitOption : options.junit()) {
            args.addJunitOption(junitOption);
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
