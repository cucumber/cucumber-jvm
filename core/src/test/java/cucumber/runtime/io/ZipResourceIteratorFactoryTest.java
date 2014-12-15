package cucumber.runtime.io;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.URL;

import org.junit.Test;

// https://github.com/cucumber/cucumber-jvm/issues/808
public class ZipResourceIteratorFactoryTest {

    // based on org.springframework.util.ResourceUtilsTests#testIsJarURL()
    // https://github.com/spring-projects/spring-framework/blob/v4.1.3.RELEASE/spring-core/src/test/java/org/springframework/util/ResourceUtilsTests.java
    @Test
    public void is_factory_for_jar_protocols() throws IOException {
        ZipResourceIteratorFactory factory = new ZipResourceIteratorFactory();

        assertTrue(factory.isFactoryFor(new URL("jar:file:myjar.jar!/mypath")));
        assertTrue(factory.isFactoryFor(new URL(null, "zip:file:myjar.jar!/mypath", new DummyURLStreamHandler())));
        assertTrue(factory.isFactoryFor(new URL(null, "wsjar:file:myjar.jar!/mypath", new DummyURLStreamHandler())));
        assertFalse(factory.isFactoryFor(new URL("file:myjar.jar")));
        assertFalse(factory.isFactoryFor(new URL("http:myserver/myjar.jar")));
    }
}
