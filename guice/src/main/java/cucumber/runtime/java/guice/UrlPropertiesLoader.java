package cucumber.runtime.java.guice;

import java.io.InputStreamReader;
import java.net.URL;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.io.IOUtils;

public class UrlPropertiesLoader {
    private final Logger logger;

    public UrlPropertiesLoader() {
        this(Logger.getLogger(UrlPropertiesLoader.class.getCanonicalName()));
    }

    public UrlPropertiesLoader(Logger logger) {
        this.logger = logger;
    }
    
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
            input.close();
        } catch (Exception e) {
            logger.log(Level.INFO, "Could not load properties file"+resource.toExternalForm(), e);
        } finally {
            IOUtils.closeQuietly(input);
        }
    }
}
