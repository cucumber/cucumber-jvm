package cucumber.runtime.java.guice.impl;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropertiesLoader {

    static Properties loadGuiceProperties() throws IOException {
        Properties properties = new Properties();
        InputStream inputStream = PropertiesLoader.class.getResourceAsStream("/cucumber-guice.properties");
        if (inputStream != null) {
            try {
                properties.load(inputStream);
            } finally {
                inputStream.close();
            }
        }
        return properties;
    }
}
