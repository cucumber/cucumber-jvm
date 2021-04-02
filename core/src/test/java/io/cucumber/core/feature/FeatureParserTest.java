package io.cucumber.core.feature;

import io.cucumber.core.gherkin.Feature;
import io.cucumber.core.resource.Resource;
import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.UUID;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsEmptyIterable.emptyIterable;
import static org.junit.jupiter.api.Assertions.*;

class FeatureParserTest {

    FeatureParser featureParser = new FeatureParser(UUID::randomUUID);

    @Test
    void renamesDeprecatedTeToTl() {
        Feature feature = featureParser.parseResource(resource("" +
                "# language: tl\n" +
                "గుణము: Test feature\n" +
                "  ఉదాహరణ: Test scenario\n" +
                "     చెప్పబడినది some text\n")).get();
        assertThat(feature.getPickles().get(0).getLanguage(), is("te"));
    }

    private Resource resource(String feature) {
        return new Resource() {
            @Override
            public URI getUri() {
                return URI.create("file:test.feature");
            }

            @Override
            public InputStream getInputStream() {
                return new ByteArrayInputStream(feature.getBytes(UTF_8));
            }
        };
    }

}
