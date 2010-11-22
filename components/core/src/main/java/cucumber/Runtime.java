package cucumber;

import cucumber.runtime.Backend;
import cucumber.runtime.ExecuteFormatter;
import gherkin.FeatureParser;
import gherkin.GherkinParser;
import gherkin.formatter.Formatter;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * A high level façade for running Cucumber features.
 */
public class Runtime {
    private final FeatureParser parser;

    public Runtime(Backend backend, Formatter formatter) {
        ExecuteFormatter executeFormatter = new ExecuteFormatter(backend, formatter);
        parser = new GherkinParser(executeFormatter);
    }

    public void execute(List<String> paths) throws IOException {
        List<FeatureSource> sources = new ArrayList<FeatureSource>();
        for (String path : paths) {
            String source = read(path);
            sources.add(new FeatureSource(source, path));
        }
        executeSources(sources);
    }

    public void executeSources(List<FeatureSource> sources) {
        for (FeatureSource source : sources) {
            source.execute(parser);
        }
    }

    private String read(String path) throws IOException {
        try {
            return read(new FileReader(path));
        } catch (FileNotFoundException e) {
            InputStream stream = getClass().getClassLoader().getResourceAsStream(path);
            return read(new InputStreamReader(stream, "UTF-8"));
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
