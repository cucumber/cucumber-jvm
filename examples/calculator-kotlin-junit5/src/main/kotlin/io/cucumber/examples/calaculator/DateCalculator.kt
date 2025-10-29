package io.cucumber.examples.calaculator

import java.time.LocalDate
import java.util.*

class DateCalculator(private val now: LocalDate) {

    fun isDateInThePast(date: LocalDate): String {
        return if (date.isBefore(now)) "yes" else "no"
    }
}

