package io.cucumber.core.plugin;

import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import io.cucumber.core.event.*;

import static java.util.Locale.ROOT;

public class UnusedStepsSummaryPrinter implements ColorAware, EventListener, SummaryPrinter {

	private final Map<String, String> unusedSteps = new TreeMap<>();
	private final NiceAppendable out;
	private Formats formats = new MonochromeFormats();

	@SuppressWarnings("WeakerAccess")
	public UnusedStepsSummaryPrinter(Appendable out) {
		this.out = new NiceAppendable(out);
	}

	@Override
	public void setEventPublisher(EventPublisher publisher) {
		// Record any steps registered
		publisher.registerHandlerFor(StepDefinedEvent.class, this::handleStepDefinedEvent);
		// Remove any steps that run
		publisher.registerHandlerFor(TestStepFinished.class, this::handleTestStepFinished);
		// Print summary when done
		publisher.registerHandlerFor(TestRunFinished.class, event -> finishReport());
	}

	private void handleStepDefinedEvent(StepDefinedEvent event) {
		unusedSteps.put(event.getStepDefinition().getLocation(false), event.getStepDefinition().getPattern());
	}

	private void handleTestStepFinished(TestStepFinished event) {
		String codeLocation = event.getTestStep().getCodeLocation();
		if (codeLocation != null) {
			unusedSteps.remove(codeLocation);
		}
	}

	private void finishReport() {
		if (unusedSteps.isEmpty()) {
			return;
		}

		Format format = formats.get(Status.UNUSED.name().toLowerCase(ROOT));
		out.println(format.text(unusedSteps.size() + " Unused steps:"));

		// Output results when done
		for (Entry<String, String> entry : unusedSteps.entrySet()) {
			String location = entry.getKey();
			String pattern = entry.getValue();
			out.println(format.text(location) + " # " + pattern);
		}
	}

	@Override
	public void setMonochrome(boolean monochrome) {
		if (monochrome) {
			formats = new MonochromeFormats();
		} else {
			formats = new AnsiFormats();
		}
	}
}
