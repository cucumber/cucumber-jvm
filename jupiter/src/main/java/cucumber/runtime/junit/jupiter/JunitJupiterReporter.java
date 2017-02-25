package cucumber.runtime.junit.jupiter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import gherkin.formatter.Formatter;
import gherkin.formatter.Reporter;
import gherkin.formatter.model.Background;
import gherkin.formatter.model.Examples;
import gherkin.formatter.model.Feature;
import gherkin.formatter.model.Match;
import gherkin.formatter.model.Result;
import gherkin.formatter.model.Scenario;
import gherkin.formatter.model.ScenarioOutline;
import gherkin.formatter.model.Step;
import gherkin.formatter.model.TagStatement;

/**
 * A Reporter and Formatter than decorates the provided classes with the
 * ability to look up results by scenario or scenario outline.
 * Not thread safe. The logic relies on a result being associated with the most
 * recently started scenario or scenario outline.
 */
public class JunitJupiterReporter implements Reporter, Formatter {
  public JunitJupiterReporter(Reporter reporter, Formatter formatter) {
    this.formatter = formatter;
    this.reporter = reporter;
  }

  public Result getResult(TagStatement statement) { return results.get(statement.getId()); }

  @Override
  public void scenarioOutline(ScenarioOutline scenarioOutline) { formatter.scenarioOutline(scenarioOutline); }

  @Override
  public void scenario(Scenario scenario) {
    formatter.scenario(scenario);
    currentStatement = scenario;
    results.put(scenario.getId(), Result.UNDEFINED);
  }

  @Override
  public void step(Step step) { formatter.step(step); }

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

  @Override
  public void syntaxError(String state, String event, List<String> legalEvents, String uri, Integer line) {
    formatter.syntaxError(state, event, legalEvents, uri, line);
  }

  @Override
  public void uri(String uri) { formatter.uri(uri); }

  @Override
  public void feature(Feature feature) { formatter.feature(feature); }

  @Override
  public void examples(Examples examples) { formatter.examples(examples); }

  @Override
  public void startOfScenarioLifeCycle(Scenario scenario) { formatter.startOfScenarioLifeCycle(scenario); }

  @Override
  public void background(Background background) { formatter.background(background); }

  @Override
  public void endOfScenarioLifeCycle(Scenario scenario) { formatter.endOfScenarioLifeCycle(scenario); }

  @Override
  public void done() { formatter.done(); }

  @Override
  public void close() { formatter.close(); }

  @Override
  public void eof() { formatter.eof(); }

  private TagStatement currentStatement = null;
  private final Formatter formatter;
  private final Reporter reporter;
  private final Map<String, Result> results = new HashMap<>();
}
