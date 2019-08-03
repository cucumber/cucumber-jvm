package io.cucumber.core.io;

import org.junit.jupiter.api.Test;

import java.net.URI;

import static io.cucumber.core.io.Helpers.jarFilePath;
import static org.junit.Assert.assertEquals;

public class HelpersTest {

    @Test
    public void computes_file_path_for_jar_protocols() throws Exception {
        assertEquals("foo bar+zap/cucumber-core.jar", jarFilePath(URI.create("jar:file:foo%20bar+zap/cucumber-core.jar!/cucumber/runtime/io")).getSchemeSpecificPart());
        assertEquals("foo bar+zap/cucumber-core.jar", jarFilePath(URI.create("zip:file:foo%20bar+zap/cucumber-core.jar!/cucumber/runtime/io")).getSchemeSpecificPart());
        assertEquals("foo bar+zap/cucumber-core.jar", jarFilePath(URI.create("wsjar:file:foo%20bar+zap/cucumber-core.jar!/cucumber/runtime/io")).getSchemeSpecificPart());
        assertEquals("foo bar+zap/cucumber-core.jar", jarFilePath(URI.create("jar:file:foo%20bar+zap/cucumber-core.jar!/")).getSchemeSpecificPart());
        assertEquals("foo bar+zap/cucumber-core.jar", jarFilePath(URI.create("zip:file:foo%20bar+zap/cucumber-core.jar!/")).getSchemeSpecificPart());
        assertEquals("foo bar+zap/cucumber-core.jar", jarFilePath(URI.create("wsjar:file:foo%20bar+zap/cucumber-core.jar!/")).getSchemeSpecificPart());
    }

}
