package cucumber.runtime.java8.test;

import cucumber.api.Configuration;
import cucumber.api.TypeRegistry;
import io.cucumber.datatable.DataTableType;
import io.cucumber.datatable.TableEntryTransformer;
import cucumber.runtime.java8.test.LambdaStepdefs.Person;

import static java.util.Locale.ENGLISH;

public class ParameterTypes implements Configuration {

    @Override
    public TypeRegistry createTypeRegistry() {
        final TypeRegistry parameterTypeRegistry = new TypeRegistry(ENGLISH);

        parameterTypeRegistry.defineDataTableType(new DataTableType(
            "person",
            Person.class,
            (TableEntryTransformer<Person>) map -> {
                Person person = new Person();
                person.first = map.get("first");
                person.last = map.get("last");
                return person;
            }));


        return parameterTypeRegistry;
    }
}
