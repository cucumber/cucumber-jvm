package io.cucumber.java8;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.Map;

public class TypeDefinitionsStepdefs implements En{
    public TypeDefinitionsStepdefs() {
        Given("docstring, defined with lambda",
            (StringBuilder builder) -> assertThat(builder.getClass(), equalTo(StringBuilder.class)));
        DocStringType("doc", (String docString) -> new StringBuilder(docString));

        DataTableType((Map<String, String> entry) -> {
            return new Author(entry.get("name"), entry.get("surname"), entry.get("famousBook"));
        });

        Given("data table, defined with lambda", (Author author) -> {
            assertThat(author.name, equalTo("Fedor"));
            assertThat(author.surname, equalTo("Dostoevsky"));
            assertThat(author.famousBook, equalTo("Crime and Punishment"));
        });

        Given("{stringbuilder} parameter, defined by lambda", (StringBuilder builder) -> {
            assertThat(builder.toString(), equalTo("stringbuilder"));
        });

        ParameterType("stringbuilder", ".*", (String str) -> new StringBuilder(str));

    }

    public static final class Author {
        private final String name;
        private final String surname;
        private final String famousBook;

        public Author(String name, String surname, String famousBook) {
            this.name = name;
            this.surname = surname;
            this.famousBook = famousBook;
        }
    }
}
