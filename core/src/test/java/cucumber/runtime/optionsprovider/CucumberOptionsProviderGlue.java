package cucumber.runtime.optionsprovider;

import cucumber.api.CucumberOptions;
import cucumber.runtime.AbstractCucumberOptionsProvider;
import java.util.HashMap;
import java.util.Map;

public class CucumberOptionsProviderGlue extends AbstractCucumberOptionsProvider {

  @Override
  public CucumberOptions getOptions() {
    Map<String, Object> options = new HashMap<String, Object>();
    options.put("glue", new String[] {"app.features.user.registration", "app.features.hooks"});
    return getCucumberOptions(options);
  }
}
