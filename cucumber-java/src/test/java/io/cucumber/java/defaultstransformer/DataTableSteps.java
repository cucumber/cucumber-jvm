package io.cucumber.java.defaultstransformer;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.cfg.ConstructorDetector;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import io.cucumber.java.DefaultDataTableCellTransformer;
import io.cucumber.java.DefaultDataTableEntryTransformer;
import io.cucumber.java.DefaultParameterTransformer;
import io.cucumber.java.en.Given;

import java.lang.reflect.Type;
import java.util.Currency;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

public final class DataTableSteps {

    private final Author expectedAuthor = new Author("Annie M. G.", "Schmidt", "1911-03-20");
    private final ObjectMapper objectMapper = JsonMapper.builder()
            .addModule(new Jdk8Module())
            .addModule(new ParameterNamesModule(JsonCreator.Mode.PROPERTIES))
            .constructorDetector(ConstructorDetector.USE_PROPERTIES_BASED)
            .build();

    @DefaultParameterTransformer
    @DefaultDataTableEntryTransformer(headersToProperties = true)
    @DefaultDataTableCellTransformer
    public Object defaultTransformer(Object fromValue, Type toValueType) {
        return objectMapper.convertValue(fromValue, objectMapper.constructType(toValueType));
    }

    @Given("a list of authors in a table")
    public void aListOfAuthorsInATable(List<Author> authors) {
        assertTrue(authors.contains(expectedAuthor));
    }

    @Given("a table with title case headers")
    public void aTableWithCapitalCaseHeaders(List<Author> authors) {
        assertTrue(authors.contains(expectedAuthor));
    }

    @Given("a single currency in a table")
    public void aSingleCurrencyInATable(Currency currency) {
        assertThat(currency, is(Currency.getInstance("EUR")));
    }

    @Given("a currency in a parameter {}")
    public void aCurrencyInAParameter(Currency currency) {
        assertThat(currency, is(Currency.getInstance("EUR")));
    }

}
