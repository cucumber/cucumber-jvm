package cucumber.io;

import org.junit.Test;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;

import static cucumber.io.ClasspathIterable.filePath;
import static org.junit.Assert.assertEquals;

public class ClasspathIterableTest {
    @Test
    public void computes_file_path_for_windows_path() throws UnsupportedEncodingException, MalformedURLException {
        if (File.separatorChar == '\\') {
            // Windows
            URL url = new URL("jar:file:/C:/src/cucumber-jvm/core/target/cucumber-core-1.0.0.RC12-SNAPSHOT.jar!/cucumber/runtime");
            assertEquals(new File("C:/src/cucumber-jvm/core/target/cucumber-core-1.0.0.RC12-SNAPSHOT.jar").getAbsolutePath(), filePath(url));
        } else {
            // POSIX
            URL url = new URL("jar:file:/src/cucumber-jvm/core/target/cucumber-core-1.0.0.RC12-SNAPSHOT.jar!/cucumber/runtime");
            assertEquals(new File("/src/cucumber-jvm/core/target/cucumber-core-1.0.0.RC12-SNAPSHOT.jar").getAbsolutePath(), filePath(url));
        }
    }

    @Test
    public void computes_file_path_for_windows_path_with_dots() throws UnsupportedEncodingException, MalformedURLException {
        if (File.separatorChar == '\\') {
            // Windows
            URL url = new URL("jar:file:C:/src/cucumber-jvm/jruby/bin/../lib/cucumber-jruby-full.jar!/cucumber/runtime");
            assertEquals(new File("C:/src/cucumber-jvm/jruby/bin/../lib/cucumber-jruby-full.jar").getAbsolutePath(), filePath(url));
        } else {
            // POSIX
            URL url = new URL("jar:file:/src/cucumber-jvm/jruby/bin/../lib/cucumber-jruby-full.jar!/cucumber/runtime");
            assertEquals(new File("/src/cucumber-jvm/jruby/bin/../lib/cucumber-jruby-full.jar").getAbsolutePath(), filePath(url));
        }
    }
}
