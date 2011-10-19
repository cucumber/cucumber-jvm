package cucumber.junit;

import cucumber.formatter.FormatterFactory;
import cucumber.formatter.NullReporter;
import gherkin.formatter.Formatter;

import java.io.FileWriter;
import java.io.IOException;

class JUnitReporterFactory {
    /**
     * Creates a JUnitReporter with an underlying gherkin reporter/formatter.
     *
     * @param reporterString what underlying reporter/formatter to create
     * @return a reporter
     */

    static JUnitReporter create(String reporterString) {
        Formatter formatter = null;
        FormatterFactory formatterFactory = new FormatterFactory();
        if (reporterString != null) {
            String[] nameAndOut = reporterString.split("=");
            String name = nameAndOut[0];
            Appendable appendable;
            try {
                if (nameAndOut.length < 2) {
                    appendable = System.out;
                } else {
                    final FileWriter fw = new FileWriter(nameAndOut[1]);
                    Runtime.getRuntime().addShutdownHook(new Thread() {
                        @Override
                        public void run() {
                            try {
                                fw.flush();
                                fw.close();
                            } catch (IOException ignore) {
                            }
                        }
                    });

                    appendable = fw;
                }
            } catch (IOException e) {
                System.err.println("ERROR: Failed to create file " + nameAndOut[0] + ". Using STDOUT instead.");
                appendable = System.out;
            }
            formatter = formatterFactory.createFormatter(name, appendable);
        }
        if (formatter == null) {
            formatter = new NullReporter();
        }
        return new JUnitReporter(formatterFactory.reporter(formatter), formatter);
    }
}