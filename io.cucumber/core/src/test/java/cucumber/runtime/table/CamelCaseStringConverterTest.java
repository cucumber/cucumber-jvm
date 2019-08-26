package cucumber.runtime.table;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class CamelCaseStringConverterTest {
    @Test
    public void testTransformToJavaPropertyName() {
        CamelCaseStringConverter mapper = new CamelCaseStringConverter();
        assertEquals("Transformed Name", "userName", mapper.map("User Name"));
        assertEquals("Transformed Name", "birthDate", mapper.map("  Birth   Date\t"));
        assertEquals("Transformed Name", "email", mapper.map("email"));
    }
}
