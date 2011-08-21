package cucumber.table.java;

import org.junit.Assert;
import org.junit.Test;

public class JavaBeanPropertyHeaderMapperTest {
    @Test
    public void testTransformToJavaPropertyName() {
        JavaBeanPropertyHeaderMapper mapper = new JavaBeanPropertyHeaderMapper();
        String userName = "User Name";
        String birthDate = "  Birth   Date\t";
        String email = "email";
        Assert.assertEquals("Transformed Name", "userName", mapper.map(userName));
        Assert.assertEquals("Transformed Name", "birthDate", mapper.map(birthDate));
        Assert.assertEquals("Transformed Name", "email", mapper.map(email));
    }
}
