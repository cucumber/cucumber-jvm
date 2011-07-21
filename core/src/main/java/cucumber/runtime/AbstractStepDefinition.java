package cucumber.runtime;

import java.util.Locale;

public abstract class AbstractStepDefinition implements StepDefinition {
	// TODO: Used by StepDefinitionMatch to convert arguments before executions (float etc.)
	// Maybe pass it to run instead (taking it from the locale of feature instead).
	// For now StepDefinitions are linked to the Backend with locale agnostic
	private final Locale locale;

	public AbstractStepDefinition(Locale locale) {
		this.locale = locale;
	}

	public Locale getLocale() {
		return this.locale;
	}
}
