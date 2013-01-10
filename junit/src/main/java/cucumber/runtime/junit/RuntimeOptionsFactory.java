package cucumber.runtime.junit;

import cucumber.api.junit.Cucumber;
import cucumber.runtime.RuntimeOptions;
import cucumber.runtime.io.MultiLoader;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RuntimeOptionsFactory {
    private final Class clazz;

    public RuntimeOptionsFactory(Class clazz) {
        this.clazz = clazz;
    }

    public RuntimeOptions create() {
        List<String> args = new ArrayList<String>();
        Cucumber.Options options = getOptions(clazz);

        addDryRun(options, args);
        addMonochrome(options, args);
        addGlue(options, args, clazz);
        addTags(options, args);
        addFormats(options, args);
        addFeatures(options, args, clazz);
        addStrict(options, args);
        addName(options, args);
        addDotCucumber(options, args);

        RuntimeOptions runtimeOptions = new RuntimeOptions(System.getProperties(), args.toArray(new String[args.size()]));

        return runtimeOptions;
    }

    private void addDotCucumber(Cucumber.Options options, List<String> args) {
        if (options != null) {
            if (!options.dotcucumber().isEmpty()) {
                args.add("--dotcucumber");
                args.add(options.dotcucumber());
            }
        }
    }

    private void addName(Cucumber.Options options, List<String> args) {
        if (options != null) {
            if (options.name().length != 0) {
                for (String name : options.name()) {
                    args.add("--name");
                    args.add(name);
                }
            }
        }
    }

    private Cucumber.Options getOptions(Class<?> clazz) {
        return clazz.getAnnotation(Cucumber.Options.class);
    }

    private void addDryRun(Cucumber.Options options, List<String> args) {
        if (options != null) {
            if (options.dryRun()) {
                args.add("--dry-run");
            }
        }
    }

    private void addMonochrome(Cucumber.Options options, List<String> args) {
        if (options != null) {
            if (options.monochrome() || runningInEnvironmentWithoutAnsiSupport()) {
                args.add("--monochrome");
            }
        }
    }

    private boolean runningInEnvironmentWithoutAnsiSupport() {
        boolean intelliJidea = System.getProperty("idea.launcher.bin.path") != null;
        // TODO: What does Eclipse use?
        return intelliJidea;
    }

    private void addGlue(Cucumber.Options options, List<String> args, Class clazz) {
        if (options != null && options.glue().length != 0) {
            for (String glue : options.glue()) {
                args.add("--glue");
                args.add(glue);
            }
        } else {
            args.add("--glue");
            args.add(MultiLoader.CLASSPATH_SCHEME + packagePath(clazz));
        }
    }

    private void addTags(Cucumber.Options options, List<String> args) {
        if (options != null) {
            for (String tags : options.tags()) {
                args.add("--tags");
                args.add(tags);
            }
        }
    }

    private void addFormats(Cucumber.Options options, List<String> args) {
        if (options != null && options.format().length != 0) {
            for (String format : options.format()) {
                args.add("--format");
                args.add(format);
            }
        } else {
            args.add("--format");
            args.add("null");
        }
    }

    private void addFeatures(Cucumber.Options options, List<String> args, Class clazz) {
        if (options != null && options.features().length != 0) {
            Collections.addAll(args, options.features());
        } else {
            args.add(MultiLoader.CLASSPATH_SCHEME + packagePath(clazz));
        }
    }

    private void addStrict(Cucumber.Options options, List<String> args) {
        if (options != null && options.strict()) {
            args.add("--strict");
        }
    }

    static String packagePath(Class clazz) {
        return packagePath(packageName(clazz.getName()));
    }

    static String packagePath(String packageName) {
        return packageName.replace('.', '/');
    }

    static String packageName(String className) {
        return className.substring(0, Math.max(0, className.lastIndexOf(".")));
    }

}
