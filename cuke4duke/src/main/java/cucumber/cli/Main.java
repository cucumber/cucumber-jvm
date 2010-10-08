package cucumber.cli;

import cucumber.Runtime;
import cucumber.runtime.Backend;
import cucumber.runtime.java.JavaBackend;
import cucumber.runtime.java.ObjectFactory;
import cucumber.runtime.java.picocontainer.PicoFactory;
import gherkin.formatter.Formatter;
import gherkin.formatter.PrettyFormatter;

import java.io.IOException;
import java.io.Writer;

/**
 * Command line interface around {@link cucumber.Runtime}
 */
public class Main {
    public void execute(Writer out, String stepDefPackage, String[] features) throws IOException {
        ObjectFactory objectFactory = new PicoFactory();
        Backend backend = new JavaBackend(objectFactory, stepDefPackage);
        Formatter formatter = new PrettyFormatter(out, true);
        Runtime runtime = new Runtime(backend, formatter);
        runtime.execute(features);
    }
}
