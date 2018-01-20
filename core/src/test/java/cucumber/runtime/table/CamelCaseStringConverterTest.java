package cucumber.runtime.table;

import cucumber.runtime.CucumberException;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class CamelCaseStringConverterTest {

    private final CamelCaseStringConverter mapper = new CamelCaseStringConverter();

    @Test
    public void testTransformToJavaPropertyName() {

        assertEquals("Transformed Name", "userName", mapper.map("User Name"));
        assertEquals("Transformed Name", "birthDate", mapper.map("  Birth   Date\t"));
        assertEquals("Transformed Name", "email", mapper.map("email"));
    }

    @Test(expected = CucumberException.class)
    public void testEmptyInputShouldBeRejected() {
        assertEquals("", mapper.map(" "));
    }

}
