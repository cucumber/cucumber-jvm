package cucumber.api.junit.jupiter;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import cucumber.api.CucumberOptions;
import cucumber.api.junit.jupiter.CucumberExtension;
import org.junit.jupiter.api.DynamicContainer;
import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.extension.ExtendWith;

import static java.util.stream.Collectors.toList;
import static org.junit.jupiter.api.Assertions.assertEquals;

@CucumberOptions(
    plugin = {"pretty"},
    features = {"classpath:cucumber.runtime.junit.jupiter"}
)
public class CucumberExtensionTest {
    @ExtendWith(CucumberExtension.class)
    @TestFactory
    public Stream<DynamicContainer> runCukes(Stream<DynamicContainer> tests) {
        List<DynamicContainer> features = tests.collect(toList());
        assertEquals(features.size(), 1);
        assertEquals(features.get(0).getDisplayName(), "FA");
        return features.stream();
    }
}
