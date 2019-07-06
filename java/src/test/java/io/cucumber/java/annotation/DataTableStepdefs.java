package io.cucumber.java.annotation;

import io.cucumber.java.DataTableType;
import io.cucumber.java.Transpose;
import io.cucumber.java.en.Given;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class DataTableStepdefs {

    private final Author expectedAuthor = new Author("Annie M. G.", "Schmidt", "1911-03-20");
    private final Person expectedPerson = new Person("Astrid", "Lindgren");

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
        public String toString() {
            return "Author{" +
                "firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", birthDate='" + birthDate + '\'' +
                '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Author author = (Author) o;

            if (!firstName.equals(author.firstName)) return false;
            if (!lastName.equals(author.lastName)) return false;
            return birthDate.equals(author.birthDate);
        }

        @Override
        public int hashCode() {
            int result = firstName.hashCode();
            result = 31 * result + lastName.hashCode();
            result = 31 * result + birthDate.hashCode();
            return result;
        }
    }

    @Given("a list of people in a table")
    public void this_table_of_authors(List<DataTableStepdefs.Person> persons) {
        assertTrue(persons.contains(expectedPerson));
    }

    @DataTableType
    public DataTableStepdefs.Person transform(Map<String, String> tableEntry) {
        return new Person(tableEntry.get("first"), tableEntry.get("last"));
    }

    public static class Person {
        private final String first;
        private final String last;

        public Person(String first, String last) {
            this.first = first;
            this.last = last;
        }


        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Person person = (Person) o;
            return first.equals(person.first) &&
                last.equals(person.last);
        }

        @Override
        public int hashCode() {
            return Objects.hash(first, last);
        }


    }


}
