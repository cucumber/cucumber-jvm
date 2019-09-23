package io.cucumber.java8;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import io.cucumber.datatable.DataTable;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class TypeDefinitionsStepdefs implements En{
    public TypeDefinitionsStepdefs() {
        Given("docstring, defined by lambda",
            (StringBuilder builder) -> assertThat(builder.getClass(), equalTo(StringBuilder.class)));
        DocStringType("doc", (String docString) -> new StringBuilder(docString));

        DataTableType((Map<String, String> entry) -> {
            return new Author(entry.get("name"), entry.get("surname"), entry.get("famousBook"));
        });

        Given("single entry data table, defined by lambda", (Author author) -> {
            assertThat(author.name, equalTo("Fedor"));
            assertThat(author.surname, equalTo("Dostoevsky"));
            assertThat(author.famousBook, equalTo("Crime and Punishment"));
        });

        Given("data table, defined by lambda", (DataTable dataTable) -> {
            List<Author> authors = dataTable.asList(Author.class);
            Author dostoevsky = new Author("Fedor","Dostoevsky", "Crime and Punishment");
            Author tolstoy = new Author("Lev", "Tolstoy", "War and Peace");
            assertThat(authors.get(0), equalTo(dostoevsky));
            assertThat(authors.get(1), equalTo(tolstoy));
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

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Author author = (Author) o;
            return Objects.equals(name, author.name) &&
                Objects.equals(surname, author.surname) &&
                Objects.equals(famousBook, author.famousBook);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, surname, famousBook);
        }
    }
}
