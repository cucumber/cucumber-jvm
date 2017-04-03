package cucumber.runtime.junit.jupiter;

import gherkin.formatter.Reporter;
import gherkin.formatter.model.Result;
import gherkin.formatter.model.Scenario;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;

public class JunitJupiterReporterTest {
  @Test
  void scenarioResultsShouldBeRecorded() {
    Reporter reporter = mock(Reporter.class);
    JunitJupiterReporter jupiterReporter = new JunitJupiterReporter(reporter);

    Scenario frank = new Scenario(null, null, null,
            "Frank the scenario", null, 1, "scenario-frank");
    jupiterReporter.scenario(frank);
    Result frankResult = new Result(Result.PASSED, 1L, null);
    jupiterReporter.result(frankResult);

    Scenario annette = new Scenario(null, null, null,
            "Annette the scenario", null, 1, "scenario-annette");
    jupiterReporter.scenario(annette);
    Result annetteResult = new Result(Result.FAILED, 1L, new Exception("It broke"), null);
    jupiterReporter.result(annetteResult);

    Scenario shuxiang = new Scenario(null, null, null,
            "Shuxiang the scenario", null, 1, "scenario-shuxiang");
    jupiterReporter.scenario(shuxiang);
    jupiterReporter.result(Result.SKIPPED);

    Scenario ahmed = new Scenario(null, null, null,
            "Ahmed the scenario", null, 1, "scenario-ahmed");

    assertEquals(jupiterReporter.getResult(frank), frankResult);
    assertEquals(jupiterReporter.getResult(annette), annetteResult);
    assertEquals(jupiterReporter.getResult(shuxiang), Result.SKIPPED);
    assertNull(jupiterReporter.getResult(ahmed));
  }

}
