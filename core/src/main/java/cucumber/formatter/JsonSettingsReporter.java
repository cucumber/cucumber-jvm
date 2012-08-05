package cucumber.formatter;

import gherkin.deps.com.google.gson.Gson;
import gherkin.deps.com.google.gson.GsonBuilder;
import gherkin.formatter.Formatter;
import gherkin.formatter.NiceAppendable;
import gherkin.formatter.model.Background;
import gherkin.formatter.model.Examples;
import gherkin.formatter.model.Feature;
import gherkin.formatter.model.Scenario;
import gherkin.formatter.model.ScenarioOutline;
import gherkin.formatter.model.Step;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class JsonSettingsReporter implements ContextAware, Formatter {

	public static class Environment {
		private Map<String, Object> systemProperties;
		private Map<String, Object> environmentVariables;
		private String[] cucumberArguments;

		public Map<String, Object> getSystemProperties() {
			return systemProperties;
		}

		public void setSystemProperties(Map<String, Object> systemProperties) {
			this.systemProperties = systemProperties;
		}

		public Map<String, Object> getEnvironmentVariables() {
			return environmentVariables;
		}

		public void setEnvironmentVariables(
				Map<String, Object> environmentVariables) {
			this.environmentVariables = environmentVariables;
		}

		public String[] getCucumberArguments() {
			return cucumberArguments;
		}

		public void setCucumberArguments(String[] cucumberArguments) {
			this.cucumberArguments = cucumberArguments;
		}
	}

	private final NiceAppendable out;
	private Map<String, Object> context = new HashMap<String, Object>();
    private Environment environment = null;
	
	
	public JsonSettingsReporter(NiceAppendable out) {
		this.out = out;
	}

	private Map<String, Object> getPropertiesAsMap() {
		Map<String, Object> output = new HashMap<String, Object>();
		for (Entry<Object, Object> entry : System.getProperties().entrySet()) {
			output.put(entry.getKey().toString(), entry.getValue());
		}
		return output;
	}

	public void appendSystemProperties() {
		environment.setSystemProperties(getPropertiesAsMap());
	}

	public void appendEnvironmentVariables() {
		HashMap<String, Object> env = new HashMap<String, Object>();
		env.putAll(System.getenv());
		environment.setEnvironmentVariables(env);
	}

	public void buildEnvironmentConfiguration() {
		appendSystemProperties();
		appendEnvironmentVariables();
		appendCucumberOptions();
	}

	public void appendCucumberOptions() {
		// TODO: round trip the options???? is this the proper place to
		// configure a cucumber-jvm run????
		final String[] args = (String[]) context.get("args");
		environment.setCucumberArguments(args);
	}

	public void writeJsonFile() {
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		out.append(gson.toJson(environment));
	}

	@Override
	public void setContext(Map<String, Object> context) {
		this.context = context;
		// extracting configuration here to prevent accidental (intentional
		// system/env property changes from being picked up
		// in the interest of being able to round trip.
		environment = new Environment();
		buildEnvironmentConfiguration();
	}

	/* adhere to reporting interface */

	@Override
	public void uri(String uri) {
		// a no-op... this method will get reflected in to by cucumber during
		// execution (via proxies) so we can't throw a warning
	}

	@Override
	public void feature(Feature feature) {
		// a no-op... this method will get reflected in to by cucumber during
		// execution (via proxies) so we can't throw a warning
	}

	@Override
	public void background(Background background) {
		// a no-op... this method will get reflected in to by cucumber during
		// execution (via proxies) so we can't throw a warning
	}

	@Override
	public void scenario(Scenario scenario) {
		// a no-op... this method will get reflected in to by cucumber during
		// execution (via proxies) so we can't throw a warning
	}

	@Override
	public void scenarioOutline(ScenarioOutline scenarioOutline) {
		// a no-op... this method will get reflected in to by cucumber during
		// execution (via proxies) so we can't throw a warning
	}

	@Override
	public void examples(Examples examples) {
		// a no-op... this method will get reflected in to by cucumber during
		// execution (via proxies) so we can't throw a warning
	}

	@Override
	public void step(Step step) {
		// a no-op... this method will get reflected in to by cucumber during
		// execution (via proxies) so we can't throw a warning
	}

	@Override
	public void eof() {
		// a no-op... this method will get reflected in to by cucumber during
		// execution (via proxies) so we can't throw a warning
	}

	@Override
	public void syntaxError(String state, String event,
			List<String> legalEvents, String uri, Integer line) {
		// a no-op... this method will get reflected in to by cucumber during
		// execution (via proxies) so we can't throw a warning
	}

	@Override
	public void done() {
		// ahh here we go, the one method I need to have executed.
		writeJsonFile();
	}

	@Override
	public void close() {
		// a no-op... this method will get reflected in to by cucumber during
		// execution (via proxies) so we can't throw a warning
	}

}