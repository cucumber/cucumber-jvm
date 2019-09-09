package io.cucumber.core.io;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.net.URI;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.jupiter.api.Assertions.assertAll;

public class FileResourceTest {

    @Test
    public void for_classpath_files_get_path_should_return_relative_path_from_classpath_root() {
        FileResource toTest1 = FileResource.createClasspathFileResource(new File("/testPath"), new File("/testPath/test1/test.feature"));
        FileResource toTest2 = FileResource.createClasspathFileResource(new File("testPath"), new File("testPath/test1/test.feature"));

        assertAll("Checking FileResource",
            () -> assertThat(toTest1.getPath(), is(equalTo(URI.create("classpath:test1/test.feature")))),
            () -> assertThat(toTest2.getPath(), is(equalTo(URI.create("classpath:test1/test.feature"))))
        );
    }

    @Test
    public void for_filesystem_files_get_path_should_return_the_path() {
        // setup
        FileResource toTest1 = FileResource.createFileResource(new File("test1"), new File("test1/test.feature"));
        FileResource toTest2 = FileResource.createFileResource(new File("test1/test.feature"), new File("test1/test.feature"));

        assertAll("Checking FileResource",
            () -> assertThat(toTest1.getPath(), is(equalTo(URI.create("file:test.feature")))),
            () -> assertThat(toTest2.getPath(), is(equalTo(new File("test1/test.feature").toURI())))
        );
    }

}
