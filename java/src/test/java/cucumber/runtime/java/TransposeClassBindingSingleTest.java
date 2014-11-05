package cucumber.runtime.java;

import cucumber.api.CucumberOptions;
import cucumber.api.DataTable;
import cucumber.api.Transpose;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.junit.Cucumber;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
@RunWith(Cucumber.class)
@CucumberOptions(
        glue = "cucumber.runtime.java.TransposeClassBindingSingleTest$Steps",
        features = "classpath:cucumber/runtime/java/test/javabinding.feature"
)
public class TransposeClassBindingSingleTest {
    public static class Steps {
        @Given("^I do something$")
        public void i_do_something() throws Throwable {
            assertTrue(true);
        }

        @Then("^I should get$")
        public void i_should_get(final @Transpose BindClass value) throws Throwable {
            assertEquals(new BindClass("foo", 1), value);
        }
    }

    public static class BindClass {
        private String name;
        private int amount;

        public BindClass() {
            // no-ôp
        }

        public BindClass(final String name, final int amount) {
            this.name = name;
            this.amount = amount;
        }

        public void setName(final String name) {
            this.name = name;
        }

        public void setAmount(final int amount) {
            this.amount = amount;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            final BindClass bindClass = BindClass.class.cast(o);
            return amount == bindClass.amount && name.equals(bindClass.name);

        }

        @Override
        public int hashCode() {
            int result = name.hashCode();
            result = 31 * result + amount;
            return result;
        }
    }
}
