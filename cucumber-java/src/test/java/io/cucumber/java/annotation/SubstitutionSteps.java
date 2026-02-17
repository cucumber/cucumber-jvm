package io.cucumber.java.annotation;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.jspecify.annotations.Nullable;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public final class SubstitutionSteps {

    private static final Map<String, String> ROLES = Map.of(
        "Manager", "now able to manage your employee accounts",
        "Admin", "able to manage any user account on the system");

    private @Nullable String name;
    private @Nullable String role;
    private @Nullable String details;

    @Given("I have a user account with my name {string}")
    public void I_have_a_user_account_with_my_name(String name) {
        this.name = name;
    }

    @When("an Admin grants me {word} rights")
    public void an_Admin_grants_me_role_rights(String role) {
        this.role = role;
        this.details = ROLES.get(role);
    }

    @Then("I should receive an email with the body:")
    public void I_should_receive_an_email_with_the_body(String body) {
        String expected = """
                Dear %s,
                You have been granted %s rights.  You are %s. Please be responsible.
                -The Admins""".formatted(name, role, details);
        assertEquals(expected, body);
    }

}
