package cucumber.runtime.order;

 import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

 import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

 import cucumber.runtime.CucumberException;
import cucumber.runtime.RuntimeOptions;

 public class OrderTypeTest {

     @Rule
    public ExpectedException expectedException = ExpectedException.none();

     @Test
    public void ensure_ordertype_none_lower_case_is_used() {
        assertThat(OrderType.getOrderType("none"), is(OrderType.NONE));
    }

     @Test
    public void ensure_ordertype_none_upper_case_is_used() {
        assertThat(OrderType.getOrderType("NONE"), is(OrderType.NONE));
    }

     @Test
    public void ensure_ordertype_reverse_is_used() {
        assertThat(OrderType.getOrderType("reverse"), is(OrderType.REVERSE));
    }

     @Test
    public void ensure_ordertype_random_is_used() {
        assertThat(OrderType.getOrderType("random"), is(OrderType.RANDOM));
    }

     @Test
    public void ensure_invalid_ordertype_is_not_allowed() {
        expectedException.expect(CucumberException.class);
        expectedException.expectMessage("Unrecognized OrderType invalid");
        OrderType.getOrderType("invalid");
    }
 }