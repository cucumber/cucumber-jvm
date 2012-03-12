package cucumber.runtime;

import com.beust.jcommander.IStringConverter;
import com.beust.jcommander.IStringConverterFactory;
import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import cucumber.formatter.HTMLFormatter;
import cucumber.io.ResourceLoader;
import cucumber.runtime.model.CucumberFeature;
import gherkin.formatter.Formatter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static cucumber.runtime.model.CucumberFeature.load;

public class RuntimeOptions {
    @Parameter(names = {"-g", "--glue"}, description = "Where cucumber looks for step definitions and hooks.")
    public List<String> glue = new ArrayList<String>();

    @Parameter(names = {"--dotcucumber"}, description = "Where to output .cucumber files (for code completion).")
    public File dotCucumber;

    @Parameter(names = {"--dry-run"}, description = "Don't run anything, just format the features.")
    public boolean dryRun;

    @Parameter(names = {"--tags"}, description = "Only execute scenarios matching TAG_EXPRESSION.")
    public List<String> tags= new ArrayList<String>();

    @Parameter(names = {"--strict"}, description = "Fail if there are any undefined or pending steps.")
    public boolean strict;

    @Parameter(names = {"--format"}, description = "Formatter to use.")
    public List<Formatter> formatters;

    @Parameter(description = "Feature paths")
    public List<String> featurePaths;

    public RuntimeOptions(String... args) {
        JCommander cmd = new JCommander(this);
        cmd.addConverterFactory(new FormatterFactory());
        cmd.setProgramName("cucumber");
        cmd.parse(args);
    }

    public List<CucumberFeature> cucumberFeatures(ResourceLoader resourceLoader) {
        return load(resourceLoader, featurePaths, filters());
    }

    private List<Object> filters() {
        List<Object> filters = new ArrayList<Object>();
        filters.addAll(tags);
        // TODO: Add lines and patterns (names)
        return filters;
    }

    public static class FormatterFactory implements IStringConverterFactory {
        @Override
        public Class<? extends IStringConverter<?>> getConverter(Class forType) {
            if (forType.equals(Formatter.class)) return FormatterConverter.class;
            else return null;
        }
    }

    public static class FormatterConverter implements IStringConverter<Formatter> {
        @Override
        public Formatter convert(String value) {
            return new HTMLFormatter(new File("target"));
        }
    }
}
