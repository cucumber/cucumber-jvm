package cucumber.runtime.formatter;

import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

import cucumber.api.SummaryPrinter;
import cucumber.api.event.*;
import cucumber.api.formatter.NiceAppendable;

public class UnusedStepsSummaryPrinter implements EventListener, SummaryPrinter {

	private final Map<String, String> unusedSteps = new TreeMap<>();

	private NiceAppendable out;

	@SuppressWarnings("WeakerAccess") // Used by PluginFactory
	public UnusedStepsPlugin(Appendable out) {
		this.out = new NiceAppendable(out);
	}

	@Override
	public void setEventPublisher(EventPublisher publisher) {
		// Record any steps registered
		publisher.registerHandlerFor(StepDefinedEvent.class,
				event -> unusedSteps.put(event.stepDefinition.getLocation(false), event.stepDefinition.getPattern()));
		// Remove any steps that run
		publisher.registerHandlerFor(TestStepFinished.class,
				event -> Optional.ofNullable(event.testStep.getCodeLocation()).ifPresent(unusedSteps::remove));
		// Print summary when done
		publisher.registerHandlerFor(TestRunFinished.class, event -> printSummary());
	}

	private void printSummary() {
		// Output results when done
		out.append("" + unusedSteps.size()).println(" Unused steps:");
		unusedSteps.forEach((location, pattern) -> out.append(location).append(": ").println(pattern));
	}
}
