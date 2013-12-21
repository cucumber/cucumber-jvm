/**
 * 
 */
package cucumber.runtime.model;

import static java.util.Collections.EMPTY_LIST;
import gherkin.formatter.Formatter;
import gherkin.formatter.Reporter;
import gherkin.formatter.model.Feature;
import gherkin.formatter.model.Result;
import gherkin.formatter.model.Scenario;
import gherkin.formatter.model.Step;

import java.io.File;

import cucumber.runtime.Runtime;

/**
 * An extension of CucumberFeature to represent invalid features
 */
public class InvalidCucumberFeature extends CucumberFeature {
	
	public class InvalidFeatureScenario extends CucumberScenario {

		public InvalidFeatureScenario(Scenario scenario) {
			super(InvalidCucumberFeature.this, null, scenario);
		}

		/* (non-Javadoc)
		 * @see cucumber.runtime.model.CucumberScenario#run(gherkin.formatter.Formatter, gherkin.formatter.Reporter, cucumber.runtime.Runtime)
		 */
		@Override
		public void run(Formatter formatter, Reporter reporter, Runtime runtime) {
			format(formatter);
			reporter.result(new Result(Result.FAILED, 0L, error, null));
			runtime.addError(error);
		}

	}

	private final Exception error;

	/**
	 * Creates a new cucumber feature with a single scenario and step for 
	 * representing the error when formatted
	 * @param reason
	 * @param path
	 * @param error
	 */
	public InvalidCucumberFeature(String reason, String path, Exception error) {
		super(createFeature(path), path);
		scenario(createScenario(reason));
		step(createStep(error));
		this.error = error;
	}
	
	/* (non-Javadoc)
	 * @see cucumber.runtime.model.CucumberFeature#createCucumberScenario(gherkin.formatter.model.Scenario)
	 */
	@Override
	protected CucumberScenario createCucumberScenario(Scenario scenario) {
		return new InvalidFeatureScenario(scenario);
	}

	private static Feature createFeature(String path) {
		final File file = new File(path);
		return new Feature(EMPTY_LIST, EMPTY_LIST, "Feature", file.getName(), path, 0, "");
	}

	private static Scenario createScenario(String reason) {
		return new Scenario(EMPTY_LIST, EMPTY_LIST, "Error", reason, "", 0, "");
	}	

	private static Step createStep(Exception error) {
		return new Step(EMPTY_LIST, "Cause", error.getMessage(), 0, EMPTY_LIST, null);
	}
	
}
