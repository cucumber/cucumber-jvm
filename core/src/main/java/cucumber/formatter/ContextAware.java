package cucumber.formatter;

import java.util.Map;

import cucumber.runtime.RuntimeOptions;


public interface ContextAware {
	
	/**
	 * Cucumber will pass in "RuntimeOptions" of type {@link RuntimeOptions}
	 * @param context
	 */
  public void setContext(Map<String, Object> context);
}
