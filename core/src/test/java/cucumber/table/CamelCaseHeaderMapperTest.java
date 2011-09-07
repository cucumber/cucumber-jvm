package cucumber.table;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class CamelCaseHeaderMapperTest {
    @Test
    public void testTransformToJavaPropertyName() {
        CamelCaseHeaderMapper mapper = new CamelCaseHeaderMapper();
        assertEquals("Transformed Name", "userName", mapper.map("User Name"));
        assertEquals("Transformed Name", "birthDate", mapper.map("  Birth   Date\t"));
        assertEquals("Transformed Name", "email", mapper.map("email"));
    }
}
