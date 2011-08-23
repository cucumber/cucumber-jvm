package cucumber.table.java;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class JavaBeanPropertyHeaderMapperTest {
    @Test
    public void testTransformToJavaPropertyName() {
        JavaBeanPropertyHeaderMapper mapper = new JavaBeanPropertyHeaderMapper();
        assertEquals("Transformed Name", "userName", mapper.map("User Name"));
        assertEquals("Transformed Name", "birthDate", mapper.map("  Birth   Date\t"));
        assertEquals("Transformed Name", "email", mapper.map("email"));
    }
}
