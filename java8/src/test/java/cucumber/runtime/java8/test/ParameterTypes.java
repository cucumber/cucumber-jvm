package cucumber.runtime.java8.test;

import cucumber.api.Configuration;
import cucumber.api.TypeRegistry;
import io.cucumber.datatable.DataTableType;
import cucumber.runtime.java8.test.LambdaStepdefs.Person;

import java.util.Map;

import static java.util.Locale.ENGLISH;

public class ParameterTypes implements Configuration {

    @Override
    public TypeRegistry createTypeRegistry() {
        final TypeRegistry typeRegistry = new TypeRegistry(ENGLISH);

        typeRegistry.defineDataTableType(new DataTableType(
            "person",
            Person.class,
            (Map<String, String> map) -> {
                Person person = new Person();
                person.first = map.get("first");
                person.last = map.get("last");
                return person;
            }));


        return typeRegistry;
    }
}
