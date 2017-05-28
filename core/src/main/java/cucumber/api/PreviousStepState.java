package cucumber.api;

import java.util.Optional;

public class PreviousStepState {
	
	private final Optional<Object> responseFromPreviousStep;

	public PreviousStepState(Optional<Object> responseFromPreviousStep) {
		this.responseFromPreviousStep = responseFromPreviousStep;
	}

	public Optional<Object> getResponseFromPreviousStep() {
		return responseFromPreviousStep;
	}

}
