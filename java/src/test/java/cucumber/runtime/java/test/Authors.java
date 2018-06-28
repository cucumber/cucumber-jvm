package cucumber.runtime.java.test;

import cucumber.api.Transpose;
import cucumber.api.java.en.Given;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class Authors {

    private final Author expected = new Author("Annie M. G.", "Schmidt", "1911-03-20");

    @Given("a list of authors in a table")
    public void aListOfAuthorsInATable(List<Author> authors) throws Throwable {
        assertTrue(authors.contains(expected));
    }

    @Given("a list of authors in a transposed table")
    public void aListOfAuthorsInATransposedTable(@Transpose List<Author> authors) throws Throwable {
        assertTrue(authors.contains(expected));
    }

    @Given("a single author in a table")
    public void aSingleAuthorInATable(Author author) throws Throwable {
        assertEquals(expected, author);
    }

    @Given("a single author in a transposed table")
    public void aSingleAuthorInATransposedTable(@Transpose Author author) throws Throwable {
        assertEquals(expected, author);
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
}
