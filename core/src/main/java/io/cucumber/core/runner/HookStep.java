package io.cucumber.core.runner;

class HookStep implements io.cucumber.core.backend.HookStep {

    final PickleStep relatedStep;

    public HookStep(HookTestStep testStep) {
        relatedStep = new PickleStep(testStep.getRelatedTestStep());
    }

    public PickleStep getRelatedStep() {
        return relatedStep;
    }
}
