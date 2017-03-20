package cucumber.api.junit.jupiter;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import cucumber.api.CucumberOptions;
import cucumber.api.junit.jupiter.CucumberExtension;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.assertEquals;

@CucumberOptions(
        plugin = {"pretty"},
        features = {"classpath:cucumber.runtime.junit.jupiter"}
)
public class CucumberExtensionTest {
  @ExtendWith(CucumberExtension.class)
  @TestFactory
  public Stream<DynamicTest> runCukes(Stream<DynamicTest> scenarios) {
    List<DynamicTest> tests = scenarios.collect(Collectors.toList());
    assertEquals(tests.size(), 1);
    assertEquals(tests.get(0).getDisplayName(), "Scenario: SA");
    return tests.stream();
  }
}
