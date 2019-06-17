package io.cucumber.spring.commonglue;

import cucumber.api.java.After;
import cucumber.api.java.Before;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TransactionStepDefs {

    @Given("a feature with the @txn annotation")
    public void a_feature_with_the_txn_annotation() throws Throwable {
        // blank
    }

    @Then("the scenarios shall execute within a transaction")
    public void the_scenarios_shall_execute_within_a_transaction() throws Throwable {
        assertTrue("No transaction is active",
            TransactionSynchronizationManager.isActualTransactionActive());
    }

    @Before(value = "@txn", order = 99)
    public void before_transaction_scenario() {
        assertFalse("A transaction is active",
                TransactionSynchronizationManager.isActualTransactionActive());
    }

    @After(value = "@txn", order = 99)
    public void after_transaction_scenario() {
        assertFalse("A transaction is active",
                TransactionSynchronizationManager.isActualTransactionActive());
    }
}
