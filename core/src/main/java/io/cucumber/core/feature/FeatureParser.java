package io.cucumber.core.feature;

import io.cucumber.core.exception.CucumberException;
import io.cucumber.core.gherkin.CucumberFeature;
import io.cucumber.core.gherkin.CucumberFeatureParser;
import io.cucumber.core.resource.Resource;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.ServiceLoader;

import static java.util.Objects.requireNonNull;

public class FeatureParser {
    private FeatureParser() {

    }

    public static CucumberFeature parseResource(Resource resource) {
        requireNonNull(resource);
        URI uri = resource.getUri();
        String source = read(resource);
        ServiceLoader<CucumberFeatureParser> services = ServiceLoader.load(CucumberFeatureParser.class);
        Iterator<CucumberFeatureParser> iterator = services.iterator();
        List<CucumberFeatureParser> parser = new ArrayList<>();
        while (iterator.hasNext()){
            parser.add(iterator.next());
        }
        Comparator<CucumberFeatureParser> version = Comparator.comparing(CucumberFeatureParser::version);
        return Collections.max(parser,version).parse(uri, source);
    }

    private static String read(Resource resource) {
        try {
            return Encoding.readFile(resource);
        } catch (IOException e) {
            throw new CucumberException("Failed to read resource:" + resource.getUri(), e);
        }
    }


}
