package cucumber.cli;

import cucumber.formatter.FormatterFactory;
import cucumber.formatter.MultiFormatter;
import cucumber.io.FileResourceLoader;
import cucumber.runtime.Runtime;
import cucumber.runtime.snippets.SummaryPrinter;
import gherkin.formatter.Formatter;
import gherkin.formatter.Reporter;

import java.io.File;
import java.io.IOException;
import java.util.List;

// TODO: Give this class a better name.
public class RuntimeActions {

    private final DefaultRuntimeFactory runtimeFactory = new DefaultRuntimeFactory();

    public int apply(RuntimeOptions options) throws IOException {
        FormatCreator formatCreator = new FormatCreator(new FormatterFactory(), new MultiFormatter(Thread.currentThread().getContextClassLoader()));
        options.applyFormats(formatCreator);

        Runtime runtime = runtimeFactory.createRuntime(new FileResourceLoader(), options.getGluePaths(), Thread.currentThread().getContextClassLoader(), options.isDryRun());

        if (options.getDotCucumber() != null) {
            writeDotCucumber(options.getFeaturePaths(), options.getDotCucumber(), runtime);
        }

        run(options.getFeaturePaths(), options.getFilterTags(), formatCreator.getMultiFormatter(), runtime);
        printSummary(runtime);

        return runtime.exitStatus();
    }

    void writeDotCucumber(List<String> featurePaths, String dotCucumberPath, Runtime runtime) throws IOException {
        File dotCucumber = new File(dotCucumberPath);
        dotCucumber.mkdirs();
        runtime.writeStepdefsJson(featurePaths, dotCucumber);
    }

    void run(List<String> featurePaths, List<Object> filters, MultiFormatter multiFormatter, Runtime runtime) throws IOException {
        Formatter formatter = multiFormatter.formatterProxy();
        Reporter reporter = multiFormatter.reporterProxy();
        runtime.run(featurePaths, filters, formatter, reporter);
        formatter.done();
    }

    void printSummary(Runtime runtime) {
        new SummaryPrinter(System.out).print(runtime);
    }
}