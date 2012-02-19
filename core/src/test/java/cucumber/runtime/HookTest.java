package cucumber.runtime;

import cucumber.io.ClasspathResourceLoader;
import cucumber.runtime.model.CucumberFeature;
import cucumber.runtime.model.CucumberScenario;
import gherkin.formatter.Formatter;
import gherkin.formatter.Reporter;
import gherkin.formatter.model.Feature;
import gherkin.formatter.model.Scenario;
import gherkin.formatter.model.Tag;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Matchers;

import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class HookTest {

    private static final List<String> TAGS = new ArrayList<String>();
    private static final List<String> CODE_PATHS = new ArrayList<String>();

    /**
     * Test for <a href="https://github.com/cucumber/cucumber-jvm/issues/23">#23</a>.
     * TODO: ensure this is no longer needed with the alternate approach taken in Runtime
     * TODO: this test is rather brittle, since there's lots of mocking :(
     */
    @Test
    public void after_hooks_execute_before_objects_are_disposed() throws Throwable {
        Backend backend = mock(Backend.class);
        HookDefinition hook = mock(HookDefinition.class);
        when(hook.matches(anyListOf(Tag.class))).thenReturn(true);
        Scenario gherkinScenario = mock(Scenario.class);

        CucumberFeature feature = mock(CucumberFeature.class);
        Feature gherkinFeature = mock(Feature.class);

        when(feature.getFeature()).thenReturn(gherkinFeature);
        when(gherkinFeature.getTags()).thenReturn(new ArrayList<Tag>());

        CucumberScenario scenario = new CucumberScenario(feature, null, gherkinScenario);

        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        Runtime runtime = new Runtime(new ClasspathResourceLoader(classLoader), CODE_PATHS, classLoader, asList(backend), false);
        runtime.getGlue().addAfterHook(hook);

        scenario.run(mock(Formatter.class), mock(Reporter.class), runtime);

        InOrder inOrder = inOrder(hook, backend);
        inOrder.verify(hook).execute(Matchers.<ScenarioResult>any());
        inOrder.verify(backend).disposeWorld();
    }


}
