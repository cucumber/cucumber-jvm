package cuke4duke.internal;

import org.jruby.RubyException;

public interface Visitor {
    void visitFeatures();
    void visitScenarioName(String keyword, String scenarioName);
    void visitStepResult(String keyword, String status, RubyException exception);
}
