package cucumber.testng;

import gherkin.formatter.Formatter;
import gherkin.formatter.Reporter;

import org.testng.ITestResult;
import org.testng.annotations.Test;

import cucumber.formatter.CucumberPrettyFormatter;
import cucumber.runtime.Runtime;
import cucumber.runtime.model.CucumberScenario;

public class TestRunner {
	private CucumberScenario cucumberScenario;
	private Reporter reporter;
	private Runtime runtime;
	private Formatter formatter;

	public TestRunner(CucumberScenario scenario, Reporter reporter,
			Formatter formatter, Runtime runtime) {
		this.cucumberScenario = scenario;
		this.reporter = reporter;
		this.runtime = runtime;
		this.formatter = formatter;
	}

	@Test
	public void call() throws Exception {
		StringBuilder stepText = new StringBuilder();
		Formatter formatter = new CucumberPrettyFormatter(stepText);
		CapturingReporter reporter = new CapturingReporter();
		cucumberScenario.run(formatter, reporter, runtime);
		ITestResult tr = org.testng.Reporter.getCurrentTestResult();
		tr.setAttribute("Cucumber Scenario", cucumberScenario.getVisualName());
		tr.setAttribute("Results", stepText.toString());
		if (reporter.failed() != null) {
			tr.setStatus(ITestResult.FAILURE);
			tr.setThrowable(reporter.failed());
		} else if (reporter.skips() != null) {
			tr.setStatus(ITestResult.SKIP);
			tr.setThrowable(reporter.skips());
		} else {
			tr.setStatus(ITestResult.SUCCESS);
		}
	}
}
