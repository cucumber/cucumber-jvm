package cucumber.runtime.java;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class CamelCaseFunctionNameSanitizerTest {

    @Test
    public void testSanitizeFunctionName() {

        CamelCaseFunctionNameSanitizer generator = new CamelCaseFunctionNameSanitizer();

        String functionName = "I am a function name";
        String expected = "iAmAFunctionName";
        String actual = generator.sanitizeFunctionName(functionName);

        assertEquals(expected, actual);
    }
}
