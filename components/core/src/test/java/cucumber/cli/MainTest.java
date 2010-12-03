package cucumber.cli;

import cucumber.runtime.java.JavaBackendTest;
import org.junit.Test;

import java.io.IOException;
import java.io.StringWriter;

import static org.junit.Assert.assertEquals;

public class MainTest {
    @Test
    public void testShouldRunCukes() throws IOException {
        StringWriter sw = new StringWriter();
        Main.mainWithWriter(sw,
                "--stepdefs", "cucumber.runtime.java",
                "cucumber/runtime"
        );
        System.out.println(sw);
        assertEquals(JavaBackendTest.OUTPUT, sw.toString());
    }

}
