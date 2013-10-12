package cucumber.runtime.snippets;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class CamelCaseFunctionNameSanitizerTest {

    private void assertSanitized(String expected, String functionName) {
        assertEquals(expected, new CamelCaseFunctionNameSanitizer().sanitizeFunctionName(functionName));
    }

    @Test
    public void sanitizes_simple_sentence() {
        assertSanitized("iAmAFunctionName", "I am a function name");
    }

    @Test
    public void sanitizes_sentence_with_multiple_spaces() {
        assertSanitized("iAmAFunctionName", "I am a function name");
    }

    @Test
    public void sanitizes_pascal_case_word() {
        assertSanitized("functionNameWithPascalCaseWord", "Function name with pascalCase word");
    }

    @Test
    public void sanitizes_camel_case_word() {
        assertSanitized("functionNameWithCamelCaseWord", "Function name with CamelCase word");
    }

    @Test
    public void sanitizes_acronyms() {
        assertSanitized("functionNameWithMultiCharAcronymHttpServer", "Function name with multi char acronym HTTP Server");
    }

    @Test
    public void sanitizes_two_char_acronym() {
        assertSanitized("functionNameWithTwoCharAcronymUS", "Function name with two char acronym US");
    }
}
