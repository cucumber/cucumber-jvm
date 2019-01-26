package io.cucumber.core.io;

import org.junit.Test;

import java.io.File;
import java.net.URI;

import static org.junit.Assert.assertEquals;

public class FileResourceTest {

    @Test
    public void for_classpath_files_get_path_should_return_relative_path_from_classpath_root() {
        FileResource toTest1 = FileResource.createClasspathFileResource(new File("/testPath"), new File("/testPath/test1/test.feature"));
        FileResource toTest2 = FileResource.createClasspathFileResource(new File("testPath"), new File("testPath/test1/test.feature"));

        assertEquals(URI.create("classpath:test1/test.feature"), toTest1.getPath());
        assertEquals(URI.create("classpath:test1/test.feature"), toTest2.getPath());
    }

    @Test
    public void for_filesystem_files_get_path_should_return_the_path() {
        // setup
        FileResource toTest1 = FileResource.createFileResource(new File("test1"), new File("test1/test.feature"));
        FileResource toTest2 = FileResource.createFileResource(new File("test1/test.feature"), new File("test1/test.feature"));

        // test
        assertEquals(URI.create("file:test.feature"), toTest1.getPath());
        assertEquals(new File("test1/test.feature").toURI(), toTest2.getPath());
    }
}
