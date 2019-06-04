package cucumber.runtime.formatter;

import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import cucumber.api.Result;
import cucumber.api.SummaryPrinter;
import cucumber.api.event.*;
import cucumber.api.formatter.ColorAware;
import cucumber.api.formatter.NiceAppendable;

public class UnusedStepsSummaryPrinter implements ColorAware, EventListener, SummaryPrinter {

	private EventHandler<StepDefinedEvent> stepDefinedHandler = new EventHandler<StepDefinedEvent>() {
		@Override
		public void receive(StepDefinedEvent event) {
			unusedSteps.put(event.stepDefinition.getLocation(false), event.stepDefinition.getPattern());
		}
	};
	private EventHandler<TestStepFinished> testStepFinishedHandler = new EventHandler<TestStepFinished>() {
		@Override
		public void receive(TestStepFinished event) {
			String codeLocation = event.testStep.getCodeLocation();
			if (codeLocation != null) {
				unusedSteps.remove(codeLocation);
			}
		}
	};
	private EventHandler<TestRunFinished> testRunFinishedhandler = new EventHandler<TestRunFinished>() {
		@Override
		public void receive(TestRunFinished event) {
			if (unusedSteps.isEmpty()) {
				return;
			}

			Format format = formats.get(Result.Type.UNUSED.lowerCaseName());
			out.println(format.text(unusedSteps.size() + " Unused steps:"));

			// Output results when done
			for (Entry<String, String> entry : unusedSteps.entrySet()) {
				String location = entry.getKey();
				String pattern = entry.getValue();
				out.println(format.text(location) + " # " + pattern);
			}
		}
	};

	private final Map<String, String> unusedSteps = new TreeMap<>();
	private final NiceAppendable out;
	private Formats formats = new MonochromeFormats();

	@SuppressWarnings("WeakerAccess") // Used by PluginFactory
	public UnusedStepsSummaryPrinter(Appendable out) {
		this.out = new NiceAppendable(out);
	}

	@Override
	public void setEventPublisher(EventPublisher publisher) {
		// Record any steps registered
		publisher.registerHandlerFor(StepDefinedEvent.class, stepDefinedHandler);
		// Remove any steps that run
		publisher.registerHandlerFor(TestStepFinished.class, testStepFinishedHandler);
		// Print summary when done
		publisher.registerHandlerFor(TestRunFinished.class, testRunFinishedhandler);
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
