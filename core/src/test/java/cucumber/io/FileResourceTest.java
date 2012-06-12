package cucumber.io;

import java.io.File;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class FileResourceTest {

    @Test
    public void get_path_should_return_short_path_when_root_same_as_file() {
        // setup
        FileResource toTest = new FileResource(new File("test1/test.feature"), new File("test1/test.feature"));

        // test
        assertEquals("test1\\test.feature", toTest.getPath());
    }

    @Test
    public void get_path_should_return_truncated_path_when_absolute_file_paths_are_input() {
        // setup
        FileResource toTest = new FileResource(new File("c://testPath/test1"), new File("c://testPath/test1/test.feature"));

        // test
        assertEquals("test1\\test.feature", toTest.getPath());
    }
}
