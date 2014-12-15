package cucumber.runtime.io;

import static cucumber.runtime.io.ClasspathResourceIterable.filePath;
import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;

import org.junit.Test;

public class ClasspathResourceIterableTest {
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

    // based on org.springframework.util#testExtractJarFileURL()
    // https://github.com/spring-projects/spring-framework/blob/v4.1.3.RELEASE/spring-core/src/test/java/org/springframework/util/ResourceUtilsTests.java
    @Test
    public void computes_file_path_for_jar_protocols() throws Exception {
        assertEquals("myjar.jar",  filePath(new URL("jar:file:myjar.jar!/mypath")));
        assertEquals("myjar.jar",  filePath(new URL(null, "zip:file:myjar.jar!/mypath", new DummyURLStreamHandler())));
        assertEquals("myjar.jar",  filePath(new URL(null, "wsjar:file:myjar.jar!/mypath", new DummyURLStreamHandler())));
        assertEquals("myjar.jar",  filePath(new URL("jar:file:myjar.jar!/")));
        assertEquals("myjar.jar",  filePath(new URL(null, "zip:file:myjar.jar!/", new DummyURLStreamHandler())));
        assertEquals("myjar.jar",  filePath(new URL(null, "wsjar:file:myjar.jar!/", new DummyURLStreamHandler())));
        assertEquals("myjar.jar",  filePath(new URL("file:myjar.jar")));

        assertEquals(File.separatorChar + "myjar.jar", filePath(new URL(null, "jar:myjar.jar!/mypath", new DummyURLStreamHandler())));
    }
}
