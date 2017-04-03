package cucumber.runtime.junit.jupiter;

import gherkin.formatter.Reporter;
import gherkin.formatter.model.Match;
import gherkin.formatter.model.Result;
import gherkin.formatter.model.Scenario;
import gherkin.formatter.model.TagStatement;

import java.util.HashMap;
import java.util.Map;

/**
 * A Reporter that decorates the provided classes with the
 * ability to look up results by scenario or scenario outline.
 * Not thread safe. The logic relies on a result being associated with the most
 * recently started scenario or scenario outline.
 */
public class JunitJupiterReporter implements Reporter {
  public JunitJupiterReporter(Reporter reporter) {
    this.reporter = reporter;
  }

  public Result getResult(TagStatement statement) { return results.get(statement.getId()); }

  public void scenario(Scenario scenario) {
    currentStatement = scenario;
    results.put(scenario.getId(), Result.UNDEFINED);
  }

  /**
   * Called once per step.
   * @param result The result of the step that has just been executed.
   */
  @Override
  public void result(Result result) {
    reporter.result(result);
    if (currentStatement != null) {
      results.put(currentStatement.getId(), result);
      // Once a scenario has a failed step, it's failed.
      // Later steps will be recorded as skipped, but we don't want to let that change the result of the scenario.
      if (Result.FAILED.equals(result.getStatus())) { currentStatement = null; }
    }
  }

  @Override
  public void before(Match match, Result result) { reporter.before(match, result); }

  @Override
  public void after(Match match, Result result) { reporter.after(match, result); }

  /**
   * Called when a Java step is matched and about to be called.
   * @param match The match, which includes details about the feature file.
   */
  @Override
  public void match(Match match) { reporter.match(match); }

  @Override
  public void embedding(String mimeType, byte[] data) { reporter.embedding(mimeType, data); }

  @Override
  public void write(String text) { reporter.write(text); }

  private TagStatement currentStatement = null;
  private final Reporter reporter;
  private final Map<String, Result> results = new HashMap<>();
}
