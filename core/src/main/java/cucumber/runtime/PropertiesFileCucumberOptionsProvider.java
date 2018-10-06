package cucumber.runtime;

import cucumber.api.CucumberOptions;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Extension of {@link AbstractCucumberOptionsProvider} which will read Cucumber options from a
 * properties file.
 *
 * <p>All properties with a key matching any of the methods in {@link CucumberOptions} are used to
 * create an instance of {@link CucumberOptions}. All other properties are ignored.
 *
 * <p>Values which are of type String[] in {@link CucumberOptions} need to be defined as a single
 * string separated by {@link cucumber.runtime.PropertiesFileCucumberOptionsProvider.DELIMITER} in
 * the properties file.
 *
 * <p>The fully qualified path to the properties file can be passed via the `cucumberProperties`
 * System property. The default is cucumber.properties in the user.home directory.
 */
public class PropertiesFileCucumberOptionsProvider extends AbstractCucumberOptionsProvider {

  public static final String PROPERTIES_FILE_PATH_KEY = "cucumberProperties";
  public static final String DELIMITER = ",";
  private String propertiesPath;

  public PropertiesFileCucumberOptionsProvider() {
    this.propertiesPath =
        System.getProperty(
            PROPERTIES_FILE_PATH_KEY,
            System.getProperty("user.home") + File.pathSeparator + "cucumber.properties");
  }

  public CucumberOptions getOptions() {
    Properties properties = new Properties();
    try {
      properties.load(new FileInputStream(propertiesPath));
    } catch (IOException exception) {
      throw new CucumberException("Error loading properties from file", exception);
    }

    // create cucumber options from properties
    Map<String, Object> options = new HashMap<String, Object>();
    for (Method method : CucumberOptions.class.getDeclaredMethods()) {
      addOptionIfPresent(properties, options, method.getName(), method.getReturnType());
    }
    return getCucumberOptions(options);
  }

  private void addOptionIfPresent(
      Properties props, Map<String, Object> options, String key, Class<?> type) {
    String value = props.getProperty(key);
    // map property value to the appropriate type
    if (value != null && !value.isEmpty()) {
      if (type.equals(boolean.class)) {
        options.put(key, Boolean.parseBoolean(value));
      } else if (type.equals(String[].class)) {
        options.put(key, value.split(DELIMITER));
      } else if (type instanceof Class && ((Class<?>) type).isEnum()) {
        // TODO: this is not ideal as we make the assumption that the value of the enum matches its name (except case).
        options.put(key, Enum.valueOf((Class<? extends Enum>) type, value.toUpperCase()));
      } else {
        throw new UnsupportedOperationException(
            String.format(
                "%s doesn't support mapping to type %f. Only boolean and String[] are supported.",
                this.getClass().getName(), type.getClass().getName()));
      }
    }
  }
}
