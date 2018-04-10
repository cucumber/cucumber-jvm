package cucumber.runtime.java.test;

import cucumber.api.Configuration;
import cucumber.api.TypeRegistry;
import io.cucumber.datatable.DataTable;
import io.cucumber.datatable.DataTableType;
import io.cucumber.datatable.TableEntryTransformer;
import io.cucumber.datatable.TableRowTransformer;
import cucumber.runtime.java.test.Authors.Author;
import cucumber.runtime.java.test.Stepdefs.Person;
import io.cucumber.datatable.TableTransformer;

import java.util.List;
import java.util.Map;

import static java.util.Locale.ENGLISH;

public class ParameterTypes implements Configuration {

    private final TableEntryTransformer<Person> personEntryTransformer = new TableEntryTransformer<Person>() {
        @Override
        public Person transform(Map<String, String> tableEntry) {
            Person person = new Person();
            person.first = tableEntry.get("first");
            person.last = tableEntry.get("last");
            return person;
        }
    };
    private final TableEntryTransformer<Author> authorEntryTransformer = new TableEntryTransformer<Author>() {
        @Override
        public Author transform(Map<String, String> tableEntry) {
            return new Author(
                tableEntry.get("firstName"),
                tableEntry.get("lastName"),
                tableEntry.get("birthDate"));
        }
    };
    private final TableRowTransformer<Author> authorRowTransformer = new TableRowTransformer<Author>() {
        @Override
        public Author transform(List<String> tableRow) {
            return new Author(
                tableRow.get(0),
                tableRow.get(1),
                tableRow.get(2));
        }
    };

    private final TableTransformer<Author> singleAuthorTransformer = new TableTransformer<Author>() {
        @Override
        public Author transform(DataTable table) throws Throwable {
            if (table.height() == 1) {
                return authorRowTransformer.transform(table.row(0));
            }

            Map<String, String> tableEntry = table.asMaps().get(0);
            return authorEntryTransformer.transform(tableEntry);
        }
    };

    @Override
    public TypeRegistry createTypeRegistry() {
        final TypeRegistry typeRegistry = new TypeRegistry(ENGLISH);

        typeRegistry.defineDataTableType(new DataTableType(
            "author",
            Author.class,
            authorEntryTransformer,
            true));

        typeRegistry.defineDataTableType(new DataTableType(
            "simpleAuthor",
            Author.class,
            authorRowTransformer));

        typeRegistry.defineDataTableType(new DataTableType(
            "singleAuthor",
            Author.class,
            singleAuthorTransformer));

        typeRegistry.defineDataTableType(new DataTableType(
            "person",
            Person.class,
            personEntryTransformer));

        return typeRegistry;
    }
}
