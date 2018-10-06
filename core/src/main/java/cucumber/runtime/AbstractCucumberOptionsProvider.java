package cucumber.runtime;

import cucumber.api.CucumberOptions;
import io.leangen.geantyref.AnnotationFormatException;
import io.leangen.geantyref.TypeFactory;
import java.util.Map;

/** This class allows dynamically contributing Cucumber options at runtime. */
public abstract class AbstractCucumberOptionsProvider {
  
  /**
   * This default provider returns an "empty" {@link CucumberOptions} object, i.e. all values are
   * set to their default. Override this method to retrieve Cucumber options from any source
   * (properties file, database etc.)
   *
   * @return the {@link CucumberOptions} to use
   */
  public abstract CucumberOptions getOptions();

  /**
   * @param options map to hold the cucumber options
   * @return an instance of {@link CucumberOptions} based on the values in the options mpa
   */
  protected CucumberOptions getCucumberOptions(Map<String, Object> options) {
    try {
      return TypeFactory.annotation(CucumberOptions.class, options);
    } catch (AnnotationFormatException e) {
      throw new CucumberException(
          String.format(
              "Error creating %s from options map %s",
              CucumberOptions.class.getName(), options.toString()),
          e);
    }
  }
}
