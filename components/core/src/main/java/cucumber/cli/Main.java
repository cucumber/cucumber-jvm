package cucumber.cli;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import cucumber.Cucumber;
import cucumber.classpath.Classpath;
import cucumber.runtime.Backend;
import cucumber.runtime.SummaryReporter;
import gherkin.formatter.PrettyPrinterOld;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.List;

/**
 * Command line interface around {@link cucumber.Cucumber}
 */
public class Main {
    @Parameter
    private List<String> features = null;

    @Parameter(names = "--stepdefs", required = true)
    public String packagePrefix;

    public void execute(Writer out) throws IOException {
        List<Backend> backends = Classpath.instantiateSubclasses(Backend.class, packagePrefix);
        PrettyPrinterOld reporter = new PrettyPrinterOld(out, false, null); // TODO: Pass a real CucumberRuntime here...
        Cucumber cucumber = new Cucumber(backends, reporter, new SummaryReporter(out));
        cucumber.execute(features.toArray(new String[features.size()]));
    }

    public static void main(String[] argv) throws IOException {
        mainWithWriter(new OutputStreamWriter(System.out), argv);
    }

    static void mainWithWriter(Writer out, String... argv) throws IOException {
        Main main = new Main();
        new JCommander(main, argv);
        main.execute(out);
    }
}
