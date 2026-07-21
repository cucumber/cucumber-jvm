package io.cucumber.java.defaultstransformer;

import io.cucumber.java.DefaultDataTableCellTransformer;
import io.cucumber.java.DefaultDataTableEntryTransformer;
import io.cucumber.java.DefaultParameterTransformer;
import io.cucumber.java.en.Given;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.cfg.ConstructorDetector;
import tools.jackson.databind.json.JsonMapper;

import java.lang.reflect.Type;
import java.util.Currency;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

public final class DataTableSteps {

    private final Author expectedAuthor = new Author("Annie M. G.", "Schmidt", "1911-03-20");
    private final ObjectMapper objectMapper = JsonMapper.builder()
            .constructorDetector(ConstructorDetector.USE_PROPERTIES_BASED)
            .build();

    @DefaultParameterTransformer
    @DefaultDataTableEntryTransformer
    @DefaultDataTableCellTransformer
    Object defaultTransformer(Object fromValue, Type toValueType) {
        return objectMapper.convertValue(fromValue, objectMapper.constructType(toValueType));
    }

    @Given("a list of authors in a table")
    void aListOfAuthorsInATable(List<Author> authors) {
        assertTrue(authors.contains(expectedAuthor));
    }

    @Given("a table with title case headers")
    void aTableWithCapitalCaseHeaders(List<Author> authors) {
        assertTrue(authors.contains(expectedAuthor));
    }

    @Given("a single currency in a table")
    void aSingleCurrencyInATable(Currency currency) {
        assertThat(currency, is(Currency.getInstance("EUR")));
    }

    @Given("a currency in a parameter {}")
    void aCurrencyInAParameter(Currency currency) {
        assertThat(currency, is(Currency.getInstance("EUR")));
    }

}
