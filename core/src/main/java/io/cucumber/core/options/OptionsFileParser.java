package io.cucumber.core.options;

import io.cucumber.core.exception.CucumberException;
import io.cucumber.core.feature.FeatureWithLines;
import io.cucumber.core.feature.GluePath;

import java.net.URI;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static java.nio.file.Files.readAllLines;

class OptionsFileParser {
    private static final Pattern RERUN_PATH_SPECIFICATION = Pattern.compile("(?m:^| |)(.*?\\.feature(?:(?::\\d+)*))");

    private OptionsFileParser() {

    }

    static List<FeatureWithLines> parseFeatureWithLinesFile(Path path) {
        try {
            List<FeatureWithLines> featurePaths = new ArrayList<>();
            readAllLines(path).forEach(line -> {
                Matcher matcher = RERUN_PATH_SPECIFICATION.matcher(line);
                while (matcher.find()) {
                    featurePaths.add(FeatureWithLines.parse(matcher.group(1)));
                }
            });
            return featurePaths;
        } catch (Exception e) {
            throw new CucumberException(format("Failed to parse '%s'", path), e);
        }
    }

    static List<URI> parseGlueFile(Path path) {
        try {
            return readAllLines(path)
                .stream()
                .map(GluePath::parse)
                .collect(Collectors.toList());
        } catch (Exception e) {
            throw new CucumberException(format("Failed to parse'%s'", path), e);
        }
    }
}
