package cucumber.runtime.groovy;

import cucumber.StepDefinition;
import cucumber.runtime.Backend;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static groovy.util.GroovyTestCase.assertEquals;

public class GroovyBackendTest {
    @Test
    public void findsGroovyStepDefinitions() throws IOException {
        List<GroovyBackend.Script> scripts = new ArrayList<GroovyBackend.Script>();
        String path = "cucumber/runtime/groovy/stepdefs.groovy";
        GroovyBackend.Script script = new GroovyBackend.Script(reader(path), path);
        scripts.add(script);

        Backend b = new GroovyBackend(scripts);
        List<StepDefinition> stepDefinitions = b.getStepDefinitions();
        assertEquals(1, stepDefinitions.size());
        assertEquals("stepdefs.groovy:5", stepDefinitions.get(0).getLocation());
    }

    private Reader reader(String path) throws IOException {
        URL resource = getClass().getClassLoader().getResource(path);
        return new InputStreamReader(resource.openStream());
    }

}
