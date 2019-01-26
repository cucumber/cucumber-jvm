package io.cucumber.core.model;

import org.junit.Test;

import java.io.File;
import java.net.URI;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assume.assumeThat;

public class FeatureIdentifierTest {

    @Test
    public void can_parse_classpath_form(){
        URI uri = FeatureIdentifier.parse("classpath:/path/to/file.feature");
        assertEquals("classpath", uri.getScheme());
        assertEquals("/path/to/file.feature", uri.getSchemeSpecificPart());
    }

    @Test
    public void can_parse_absolute_file_form(){
        URI uri = FeatureIdentifier.parse("file:/path/to/file.feature");
        assertEquals("file", uri.getScheme());
        assertEquals("/path/to/file.feature", uri.getSchemeSpecificPart());
    }

    @Test
    public void can_parse_relative_file_form(){
        URI uri = FeatureIdentifier.parse("file:path/to/file.feature");
        assertEquals("file", uri.getScheme());
        assertEquals("path/to/file.feature", uri.getSchemeSpecificPart());
    }

    @Test
    public void can_parse_absolute_path_form(){
        URI uri = FeatureIdentifier.parse("/path/to/file.feature");
        assertEquals("file", uri.getScheme());
        assertEquals("/path/to/file.feature", uri.getSchemeSpecificPart());
    }

    @Test
    public void can_parse_relative_path_form(){
        URI uri = FeatureIdentifier.parse("path/to/file.feature");
        assertEquals("file", uri.getScheme());
        assertEquals("path/to/file.feature", uri.getSchemeSpecificPart());
    }

    @Test
    public void can_parse_windows_path_form(){
        assumeThat(File.separatorChar, is('\\')); //Requires windows

        URI uri = FeatureIdentifier.parse("path\\to\\file.feature");
        assertEquals("file", uri.getScheme());
        assertEquals("path/to/file.feature", uri.getSchemeSpecificPart());
    }


    @Test
    public void can_parse_windows_absolute_path_form(){
        assumeThat(File.separatorChar, is('\\')); //Requires windows

        URI uri = FeatureIdentifier.parse("C:\\path\\to\\file.feature");
        assertEquals("file", uri.getScheme());
        assertEquals("C:/path/to/file.feature", uri.getSchemeSpecificPart());
    }


    @Test
    public void can_parse_whitespace_in_path(){
        URI uri = FeatureIdentifier.parse("path/to the/file.feature");
        assertEquals("file", uri.getScheme());
        assertEquals("path/to the/file.feature", uri.getSchemeSpecificPart());
    }

}
