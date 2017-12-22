package cucumber.api.datatable;

import cucumber.runtime.CucumberException;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class PascalCaseStringConverterTest {

    private final PascalCaseStringConverter mapper = new PascalCaseStringConverter();

    @Test
    public void testTransformToJavaPropertyName() {

        assertEquals("Transformed Name", "UserName", mapper.map("User Name"));
        assertEquals("Transformed Name", "BirthDate", mapper.map("  Birth   Date\t"));
        assertEquals("Transformed Name", "Email", mapper.map("email"));
    }

    @Test(expected = CucumberException.class)
    public void testEmptyInputShouldBeRejected() {
        assertEquals("", mapper.map(" "));
    }

}
