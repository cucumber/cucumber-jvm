package cucumber.runtime.snippets;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class UnderscoreFunctionNameSanitizerTest {

    private UnderscoreFunctionNameSanitizer sanitizer = new UnderscoreFunctionNameSanitizer();

    @Test
    public void testSanitizeFunctionName() {
        assertEquals("_test_function_123", sanitizer.sanitizeFunctionName(".test function 123 "));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSanitizeEmptyFunctionName() {
        sanitizer.sanitizeFunctionName("");
    }

}
