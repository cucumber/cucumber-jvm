package cucumber.runtime.java.guice;

import static cucumber.runtime.Utils.closeQuietly;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class UrlPropertiesLoader_Test {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();
    private final UrlPropertiesLoader loader = new UrlPropertiesLoader();

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

    @Test(expected=LoadingPropertiesFileFailed.class)
    public void onExceptionDuringPropertiesLoading() throws Exception {
        URL noneExistingPropertiesFile = aNoneExistingFile();
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
            closeQuietly(outputStream);
        }
        return propertiesFile1.toURI().toURL();
    }
}