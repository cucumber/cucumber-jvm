package cucumber.runtime.formatter;

import cucumber.runtime.Backend;
import cucumber.runtime.BackendSupplier;
import cucumber.runtime.Runtime;
import cucumber.runtime.snippets.FunctionNameGenerator;
import gherkin.pickles.PickleStep;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONTokener;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;

import java.util.Collection;

import static java.util.Collections.singletonList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

//TODO: Rewrite the JSONFormatter tests to use the string
//TODO: testMultipleFeatures fails because pickles can now interleave between features
public class JsonParallelRuntimeTest {

    @Test
    public void testSingleFeature() throws JSONException {
        final BackendSupplier backendSupplier = new BackendSupplier() {
            @Override
            public Collection<? extends Backend> get() {
                Backend backend = mock(Backend.class);
                when(backend.getSnippet(any(PickleStep.class), anyString(), any(FunctionNameGenerator.class))).thenReturn("TEST SNIPPET");
                return singletonList(backend);
            }
        };

        StringBuilder stringBuilderA = new StringBuilder();

        Runtime.builder()
            .withArgs("--threads", "3", "src/test/resources/cucumber/runtime/formatter/JSONPrettyFormatterTest.feature")
            .withAdditionalPlugins(new JSONFormatter(stringBuilderA))
            .withBackendSupplier(backendSupplier)
            .build()
            .run();


        StringBuilder stringBuilderB = new StringBuilder();

        Runtime.builder()
            .withArgs("--threads", "1", "src/test/resources/cucumber/runtime/formatter/JSONPrettyFormatterTest.feature")
            .withAdditionalPlugins(new JSONFormatter(stringBuilderB))
            .withBackendSupplier(backendSupplier)
            .build()
            .run();

        //TODO: Maybe use https://github.com/hertzsprung/hamcrest-json instead
        JSONAssert.assertEquals(stringBuilderB.toString(), new JSONArray(new JSONTokener(stringBuilderA.toString())), false);
    }

    @Test
    public void testMultipleFeatures() throws JSONException {
        final BackendSupplier backendSupplier = new BackendSupplier() {
            @Override
            public Collection<? extends Backend> get() {
                Backend backend = mock(Backend.class);
                when(backend.getSnippet(any(PickleStep.class), anyString(), any(FunctionNameGenerator.class))).thenReturn("TEST SNIPPET");
                return singletonList(backend);
            }
        };

        StringBuilder stringBuilderA = new StringBuilder();

        Runtime.builder()
            .withArgs("--threads", "3",
                "src/test/resources/cucumber/runtime/formatter/JSONPrettyFormatterTest.feature",
                "src/test/resources/cucumber/runtime/formatter/FormatterInParallel.feature")
            .withAdditionalPlugins(new JSONFormatter(stringBuilderA))
            .withBackendSupplier(backendSupplier)
            .build()
            .run();


        StringBuilder stringBuilderB = new StringBuilder();
        Runtime.builder()
            .withArgs("--threads", "1",
                "src/test/resources/cucumber/runtime/formatter/JSONPrettyFormatterTest.feature",
                "src/test/resources/cucumber/runtime/formatter/FormatterInParallel.feature")
            .withAdditionalPlugins(new JSONFormatter(stringBuilderB))
            .withBackendSupplier(backendSupplier)
            .build()
            .run();

        System.out.println(stringBuilderA.toString());
        System.out.println("===================");
        System.out.println(stringBuilderB.toString());

        //TODO: Maybe use https://github.com/hertzsprung/hamcrest-json instead
        JSONAssert.assertEquals(stringBuilderB.toString(), new JSONArray(new JSONTokener(stringBuilderA.toString())), false);
    }


}
