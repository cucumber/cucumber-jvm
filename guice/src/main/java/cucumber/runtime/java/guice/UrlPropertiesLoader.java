package cucumber.runtime.java.guice;

import static cucumber.runtime.Utils.closeQuietly;

import java.io.InputStreamReader;
import java.net.URL;
import java.text.MessageFormat;
import java.util.Properties;

public class UrlPropertiesLoader {
    
    public Properties load(URL resource) {
        Properties properties = new Properties();
        if (null != resource) {
            initializeFrom(resource, properties);
        }
        return properties;
    }

    private void initializeFrom(URL resource, Properties properties) {
        InputStreamReader input = null;
        try {
            input = new InputStreamReader(resource.openStream());
            properties.load(input);
        } catch (Exception e) {
            String message = MessageFormat.format("Could not load properties from ''{0}''", resource.toExternalForm());
            throw new LoadingPropertiesFileFailed(message, e);
        } finally {
            closeQuietly(input);
        }
    }
}