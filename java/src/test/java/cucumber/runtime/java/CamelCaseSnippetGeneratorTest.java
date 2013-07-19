package cucumber.runtime.java;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class CamelCaseSnippetGeneratorTest {

    @Test
    public void testSanitizeFunctionName() {

        CamelCaseSnippetGenerator generator = new CamelCaseSnippetGenerator(new JavaSnippet());

        String functionName = "I am a function name";
        String expected = "iAmAFunctionName";
        String actual = generator.sanitizeFunctionName(functionName);

        assertEquals(expected, actual);
    }
}
