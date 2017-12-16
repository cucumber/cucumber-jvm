package cucumber.runtime.java8.test;

import cucumber.api.Configuration;
import cucumber.api.TypeRegistry;
import cucumber.api.datatable.DataTableType;
import cucumber.api.datatable.TableRowTransformer;
import cucumber.runtime.java8.test.LambdaStepdefs.Person;

import static java.util.Locale.ENGLISH;

public class ParameterTypes implements Configuration {

    @Override
    public TypeRegistry createTypeRegistry() {
        final TypeRegistry parameterTypeRegistry = new TypeRegistry(ENGLISH);

        parameterTypeRegistry.defineDataTableType(new DataTableType(
            "person",
            Person.class,
            (TableRowTransformer<Person>) map -> {
                Person person = new Person();
                person.first = map.get("first");
                person.last = map.get("last");
                return person;
            }));


        return parameterTypeRegistry;
    }
}
