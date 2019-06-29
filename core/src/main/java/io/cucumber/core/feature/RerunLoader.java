package io.cucumber.core.feature;

import io.cucumber.core.exception.CucumberException;
import io.cucumber.core.io.Resource;
import io.cucumber.core.io.ResourceLoader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class RerunLoader {
    private static final Pattern RERUN_PATH_SPECIFICATION = Pattern.compile("(?m:^| |)(.*?\\.feature(?:(?::\\d+)*))");

    private final ResourceLoader resourceLoader;

    public RerunLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    public List<FeatureWithLines> load(URI rerunPath) {
        Iterable<Resource> resources = resourceLoader.resources(rerunPath, null);

        if(!resources.iterator().hasNext()){
            throw new CucumberException("Rerun file did not exist: " + rerunPath);
        }

        List<FeatureWithLines> featurePaths = new ArrayList<>();
        for (Resource resource : resources) {
            String source = read(resource);
            if (!source.isEmpty()) {
                Matcher matcher = RERUN_PATH_SPECIFICATION.matcher(source);
                while (matcher.find()) {
                    featurePaths.add(FeatureWithLines.parse(matcher.group(1)));
                }
            }
        }
        return featurePaths;
    }

    private static String read(Resource resource) {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(resource.getInputStream()))) {
            return br.lines().collect(Collectors.joining(System.lineSeparator()));
        } catch (IOException e) {
            throw new CucumberException("Failed to read resource:" + resource.getPath(), e);
        }
    }
}
