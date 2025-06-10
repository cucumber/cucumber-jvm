package io.cucumber.examples.calculator

import io.cucumber.examples.calaculator.DateCalculator
import io.cucumber.java.en.Given
import io.cucumber.java.en.Then
import io.cucumber.java.en.When
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.test.assertEquals

class DateStepDefinitions {

    private lateinit var  result: String

    private lateinit var calculator: DateCalculator


    @Given("today is {}")
    fun todayIs(date: String) {
        val date1 = LocalDate.parse(date)
        calculator = DateCalculator(now = date1)
    }

    @When("I ask if {} is in the past")
    fun iAskIfDateIsInPast(date: String) {
        val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.ENGLISH)
        val date2 = LocalDate.parse(date,formatter)
        result = calculator.isDateInThePast(date2)
    }

    @Then("the result should be {string}")
    fun theResultShouldBeYes(answer: String) {
        assertEquals(answer, result)
    }
}
