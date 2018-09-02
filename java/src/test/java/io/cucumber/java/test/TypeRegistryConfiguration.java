package io.cucumber.java.test;

import cucumber.api.TypeRegistryConfigurer;
import cucumber.api.TypeRegistry;
import io.cucumber.datatable.DataTable;
import io.cucumber.datatable.DataTableType;
import io.cucumber.datatable.TableEntryTransformer;
import io.cucumber.java.test.Authors.Author;
import io.cucumber.datatable.TableTransformer;

import java.util.Locale;
import java.util.Map;

import static java.util.Locale.ENGLISH;

public class TypeRegistryConfiguration implements TypeRegistryConfigurer {

    private final TableEntryTransformer<Stepdefs.Person> personEntryTransformer = new TableEntryTransformer<Stepdefs.Person>() {
        @Override
        public Stepdefs.Person transform(Map<String, String> tableEntry) {
            Stepdefs.Person person = new Stepdefs.Person();
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
    private final TableTransformer<Author> singleAuthorTransformer = new TableTransformer<Author>() {
        @Override
        public Author transform(DataTable table) throws Throwable {
            Map<String, String> tableEntry = table.asMaps().get(0);
            return authorEntryTransformer.transform(tableEntry);
        }
    };

    @Override
    public Locale locale() {
        return ENGLISH;
    }

    @Override
    public void configureTypeRegistry(TypeRegistry typeRegistry) {
        typeRegistry.defineDataTableType(new DataTableType(
            Author.class,
            authorEntryTransformer));

        typeRegistry.defineDataTableType(new DataTableType(
            Author.class,
            singleAuthorTransformer));

        typeRegistry.defineDataTableType(new DataTableType(
            Stepdefs.Person.class,
            personEntryTransformer));
    }
}
