package cucumber.runtime.java8.test;

import cucumber.api.TypeRegistryConfigurer;
import cucumber.api.TypeRegistry;
import io.cucumber.datatable.DataTableType;
import cucumber.runtime.java8.test.LambdaStepdefs.Person;

import java.util.Locale;
import java.util.Map;

import static java.util.Locale.ENGLISH;

public class TypeRegistryConfiguration implements TypeRegistryConfigurer {

    @Override
    public Locale locale() {
        return ENGLISH;
    }

    @Override
    public void configureTypeRegistry(TypeRegistry typeRegistry) {
        typeRegistry.defineDataTableType(new DataTableType(
            Person.class,
            (Map<String, String> map) -> {
                Person person = new Person();
                person.first = map.get("first");
                person.last = map.get("last");
                return person;
            }));
    }
}
