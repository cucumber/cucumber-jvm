package cucumber;

import cucumber.runtime.Backend;
import cucumber.runtime.Executor;
import gherkin.formatter.Formatter;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.List;

/**
 * A high level façade for running Cucumber features.
 */
public class Runtime {
    private final Executor executor;

    public Runtime(Backend backend, Formatter formatter) {
        List<StepDefinition> stepDefinitions = backend.getStepDefinitions();
        executor = new Executor(backend, stepDefinitions, formatter);
    }

    public void execute(List<String> featurePaths) throws IOException {
        String featurePath = featurePaths.get(0);
        String source = read(featurePath);
        FeatureSource featureSource = new FeatureSource(source, featurePath);
        executor.execute(featureSource);
    }

    private String read(String path) throws IOException {
        try {
            return read(new FileReader(path));
        } catch(FileNotFoundException e) {
            InputStream stream = getClass().getClassLoader().getResourceAsStream(path);
            if(stream != null) {
                return read(new InputStreamReader(stream, "UTF-8"));
            } else {
                throw new IOException("Could not find " + path + " on file or in class path.");
            }
        }
    }

    private String read(Reader reader) throws IOException {
        StringBuffer sb = new StringBuffer();
        int n;
        while ((n = reader.read()) != -1) {
            sb.append((char) n);
        }
        return sb.toString();
    }
}
