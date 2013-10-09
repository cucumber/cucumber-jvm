package cucumber.runtime.snippets;

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

        functionName = "Function name with multiple  spaces";
        expected = "functionNameWithMultipleSpaces";
        actual = generator.sanitizeFunctionName(functionName);
        assertEquals(expected, actual);

        functionName = "Function name with pascalCase word";
        expected = "functionNameWithPascalCaseWord";
        actual = generator.sanitizeFunctionName(functionName);
        assertEquals(expected, actual);

        functionName = "Function name with CamelCase word";
        expected = "functionNameWithCamelCaseWord";
        actual = generator.sanitizeFunctionName(functionName);
        assertEquals(expected, actual);

        functionName = "Function name with multi char acronym HTTP Server";
        expected = "functionNameWithMultiCharAcronymHttpServer";
        actual = generator.sanitizeFunctionName(functionName);
        assertEquals(expected, actual);

        functionName = "Function name with two char acronym US";
        expected = "functionNameWithTwoCharAcronymUS";
        actual = generator.sanitizeFunctionName(functionName);
        assertEquals(expected, actual);
    }
}
