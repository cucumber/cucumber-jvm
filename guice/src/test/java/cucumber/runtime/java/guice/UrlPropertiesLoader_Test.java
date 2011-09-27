package cucumber.runtime.java.guice;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.contains;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.io.IOUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class UrlPropertiesLoader_Test {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();
    public final Logger logger = mock(Logger.class);
    private final UrlPropertiesLoader loader = new UrlPropertiesLoader(logger);

    @Test
    public void returnsEmptyPropertiesIfNullIsPassedAsResource() throws Exception {
        URL resource = null;
        Properties properties = loader.load(resource);
        assertThat(properties.isEmpty(), is(true));
    }

    @Test
    public void loadsThePropertiesFileFromTheProvidedResource() throws Exception {
        Properties originalProperties = new Properties();
        originalProperties.put("key", "value");
        URL propertiesFileUrl = writeToAFile(originalProperties);

        Properties loadedProperties = loader.load(propertiesFileUrl);
        assertThat(loadedProperties, is(originalProperties));
    }

    @Test
    public void logsOutputIfProvidedUrlDoesNotPointToAPropertyFile() throws Exception {
        URL noneExistingPropertiesFile = aNoneExistingFile();
        loader.load(noneExistingPropertiesFile);
        verify(logger).log(eq(Level.INFO), contains("Could not load properties file"), any(Throwable.class));
    }
    
    @Test
    public void testName() throws Exception {
        URL noneExistingPropertiesFile = aNoneExistingFile();
        UrlPropertiesLoader loader = new UrlPropertiesLoader();
        loader.load(noneExistingPropertiesFile);
    }

    private URL aNoneExistingFile() throws IOException, MalformedURLException {
        File file = folder.newFile("some.properties");
        URL url = file.toURI().toURL();
        file.delete();
        return url;
    }

    private URL writeToAFile(Properties propertiesWithContent) throws IOException, FileNotFoundException, MalformedURLException {
        File propertiesFile1 = folder.newFile("some.properties");
        FileOutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream(propertiesFile1);
            propertiesWithContent.store(outputStream, "a comment");
        } finally {
            IOUtils.closeQuietly(outputStream);
        }
        return propertiesFile1.toURI().toURL();
    }
}