package io.cucumber.java.annotation;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.DataTableType;
import io.cucumber.java.Transpose;
import io.cucumber.java.en.Given;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DataTableSteps {

    private final Author expectedAuthor = new Author("Annie M. G.", "Schmidt", "1911-03-20");
    private final Person expectedPerson = new Person("Astrid", "Lindgren");
    private final Person mononymousPerson = new Person("Plato", "");

    @DataTableType
    public Author singleAuthorTransformer(DataTable table) {
        return authorEntryTransformer(table.asMaps().get(0));
    }

    @DataTableType
    public Author authorEntryTransformer(Map<String, String> entry) {
        return new DataTableSteps.Author(
            entry.get("firstName"),
            entry.get("lastName"),
            entry.get("birthDate"));
    }

    @Given("a list of authors in a table")
    public void aListOfAuthorsInATable(List<Author> authors) {
        assertTrue(authors.contains(expectedAuthor));
    }

    @Given("a list of authors in a transposed table")
    public void aListOfAuthorsInATransposedTable(@Transpose List<Author> authors) {
        assertTrue(authors.contains(expectedAuthor));
    }

    @Given("a single author in a table")
    public void aSingleAuthorInATable(Author author) {
        assertEquals(expectedAuthor, author);
    }

    @Given("a single author in a transposed table")
    public void aSingleAuthorInATransposedTable(@Transpose Author author) {
        assertEquals(expectedAuthor, author);
    }

    @Given("a list of people in a table")
    public void this_table_of_authors(List<DataTableSteps.Person> persons) {
        assertTrue(persons.contains(expectedPerson));
        assertTrue(persons.contains(mononymousPerson));
    }

    @DataTableType(replaceWithEmptyString = "[blank]")
    public DataTableSteps.Person transform(Map<String, String> tableEntry) {
        return new Person(tableEntry.get("first"), tableEntry.get("last"));
    }

    public static class Author {

        final String firstName;
        final String lastName;
        final String birthDate;

        Author(String firstName, String lastName, String birthDate) {
            this.firstName = firstName;
            this.lastName = lastName;
            this.birthDate = birthDate;
        }

        @Override
        public int hashCode() {
            int result = firstName.hashCode();
            result = 31 * result + lastName.hashCode();
            result = 31 * result + birthDate.hashCode();
            return result;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;

            Author author = (Author) o;

            if (!firstName.equals(author.firstName))
                return false;
            if (!lastName.equals(author.lastName))
                return false;
            return birthDate.equals(author.birthDate);
        }

        @Override
        public String toString() {
            return "Author{" +
                    "firstName='" + firstName + '\'' +
                    ", lastName='" + lastName + '\'' +
                    ", birthDate='" + birthDate + '\'' +
                    '}';
        }

    }

    public static class Person {

        private final String first;
        private final String last;

        public Person(String first, String last) {
            this.first = first;
            this.last = last;
        }

        @Override
        public int hashCode() {
            return Objects.hash(first, last);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;
            Person person = (Person) o;
            return first.equals(person.first) &&
                    last.equals(person.last);
        }

    }

}
