package cucumber.junit;

import cucumber.runtime.RuntimeOptions;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static cucumber.runtime.Utils.packagePath;

public class RuntimeOptionsFactory {
    private Class clazz;

    public RuntimeOptionsFactory(Class clazz) {
        this.clazz = clazz;
    }

    public RuntimeOptions create() {
        List<String> args = new ArrayList<String>();
        Cucumber.Options cucumberOptions = getFeatureAnnotation(clazz);

        addDryRun(cucumberOptions, args);
        addGlue(cucumberOptions, clazz, args);
        addFeatures(cucumberOptions, clazz, args);
        addTags(cucumberOptions, args);

        return new RuntimeOptions(args.toArray(new String[args.size()]));

    }

    private Cucumber.Options getFeatureAnnotation(Class clazz) {
        Annotation annotation = clazz.getAnnotation(Cucumber.Options.class);
        return Cucumber.Options.class.cast(annotation);
    }

    private void addDryRun(Cucumber.Options options, List<String> args) {
        if(options != null) {
            if(options.dryRun()) {
                args.add("--dry-run");
            }
        }
    }

    private void addGlue(Cucumber.Options options, Class clazz, List<String> args) {
        if(options != null) {
            for (String glue : options.glue()) {
                args.add("--glue");
                args.add(glue);
            }
        } else {
            args.add("--glue");
            args.add(packagePath(clazz));
        }
    }

    private void addFeatures(Cucumber.Options options, Class clazz, List<String> args) {
        if(options != null) {
            Collections.addAll(args, options.features());
        } else {
            args.add(packagePath(clazz));
        }
    }

    private void addTags(Cucumber.Options options, List<String> args) {
        if(options != null) {
            for (String tags : options.tags()) {
                args.add("--tags");
                args.add(tags);
            }
        }
    }

}
