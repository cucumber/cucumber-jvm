package cucumber.runtime.java.test;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class SubstitutionStepdefs {
    private static final Map<String, String> ROLES = new HashMap<String, String>() {{
        put("Manager", "now able to manage your employee accounts");
        put("Admin", "able to manage any user account on the system");
    }};

    private String name;
    private String role;
    private String details;

    @Given("^I have a user account with my name \"([^\"]*)\"$")
    public void I_have_a_user_account_with_my_name(String name) throws Throwable {
        this.name = name;
    }

    @When("^an Admin grants me (.+) rights$")
    public void an_Admin_grants_me_role_rights(String role) throws Throwable {
        this.role = role;
        this.details = ROLES.get(role);
    }

    @Then("^I should receive an email with the body:$")
    public void I_should_receive_an_email_with_the_body(String body) throws Throwable {
        String expected = String.format("Dear %s,\n" +
                "You have been granted %s rights.  You are %s. Please be responsible.\n" +
                "-The Admins", name, role, details);
        assertEquals(expected, body);
    }
}
