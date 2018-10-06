package cucumber.runtime.optionsprovider;

import cucumber.api.CucumberOptions;
import cucumber.runtime.AbstractCucumberOptionsProvider;
import java.util.HashMap;
import java.util.Map;

public class CucumberOptionsProviderStrict extends AbstractCucumberOptionsProvider{

  @Override
  public CucumberOptions getOptions() {
    Map<String, Object> options = new HashMap<String, Object>();
    options.put("strict", true);
    return getCucumberOptions(options);
  }
  
}
