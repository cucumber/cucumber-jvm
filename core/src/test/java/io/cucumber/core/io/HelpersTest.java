package io.cucumber.core.io;

import org.junit.jupiter.api.Test;

import java.net.URI;

import static io.cucumber.core.io.Helpers.jarFilePath;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.jupiter.api.Assertions.assertAll;

public class HelpersTest {

    @Test
    public void computes_file_path_for_jar_protocols() {

        assertAll("Checking Helpers.jarFilePath",
            () -> assertThat(jarFilePath(URI.create("jar:file:foo%20bar+zap/cucumber-core.jar!/cucumber/runtime/io")).getSchemeSpecificPart(), is(equalTo("foo bar+zap/cucumber-core.jar"))),
            () -> assertThat(jarFilePath(URI.create("zip:file:foo%20bar+zap/cucumber-core.jar!/cucumber/runtime/io")).getSchemeSpecificPart(), is(equalTo("foo bar+zap/cucumber-core.jar"))),
            () -> assertThat(jarFilePath(URI.create("wsjar:file:foo%20bar+zap/cucumber-core.jar!/cucumber/runtime/io")).getSchemeSpecificPart(), is(equalTo("foo bar+zap/cucumber-core.jar"))),
            () -> assertThat(jarFilePath(URI.create("jar:file:foo%20bar+zap/cucumber-core.jar!/")).getSchemeSpecificPart(), is(equalTo("foo bar+zap/cucumber-core.jar"))),
            () -> assertThat(jarFilePath(URI.create("zip:file:foo%20bar+zap/cucumber-core.jar!/")).getSchemeSpecificPart(), is(equalTo("foo bar+zap/cucumber-core.jar"))),
            () -> assertThat(jarFilePath(URI.create("wsjar:file:foo%20bar+zap/cucumber-core.jar!/")).getSchemeSpecificPart(), is(equalTo("foo bar+zap/cucumber-core.jar")))
        );
    }

}
