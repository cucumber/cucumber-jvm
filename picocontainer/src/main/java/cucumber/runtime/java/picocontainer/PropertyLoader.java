package cucumber.runtime.java.picocontainer;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropertyLoader {

    private String resource;

    public PropertyLoader(String resource) {
        this.resource = resource;
    }

    public Properties getProperties() throws IOException {
        Properties properties = new Properties();
        InputStream inputStream = PropertyLoader.class.getResourceAsStream(resource);
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
