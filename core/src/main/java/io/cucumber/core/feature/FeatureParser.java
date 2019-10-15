package io.cucumber.core.feature;

import io.cucumber.core.exception.CucumberException;
import io.cucumber.core.gherkin.CucumberFeature;
import io.cucumber.core.gherkin.CucumberFeatureParser;
import io.cucumber.core.io.Resource;

import java.io.IOException;
import java.net.URI;
import java.util.ServiceLoader;

import static java.util.Objects.requireNonNull;

public class FeatureParser {
    private FeatureParser() {

    }

    public static CucumberFeature parseResource(Resource resource) {
        requireNonNull(resource);
        URI path = resource.getPath();
        String source = read(resource);
        ServiceLoader<CucumberFeatureParser> parser = ServiceLoader.load(CucumberFeatureParser.class);
        return parser.iterator().next().parse(path, source);
    }

    private static String read(Resource resource) {
        try {
            return Encoding.readFile(resource);
        } catch (IOException e) {
            throw new CucumberException("Failed to read resource:" + resource.getPath(), e);
        }
    }


}
