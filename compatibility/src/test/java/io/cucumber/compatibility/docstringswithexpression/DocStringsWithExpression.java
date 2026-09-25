package io.cucumber.compatibility.docstringswithexpression;

import io.cucumber.docstring.DocString;
import io.cucumber.java.en.Given;

import static org.junit.jupiter.api.Assertions.assertEquals;

public final class DocStringsWithExpression {

    @Given("a {string} with a doc string:")
    public void docString(String string, DocString docString) {
        assertEquals("Cucumber", string);
        assertEquals("Cucumis sativus", docString.getContent());
    }

}
