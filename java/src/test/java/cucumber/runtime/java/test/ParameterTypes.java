package cucumber.runtime.java.test;

import cucumber.api.Configuration;
import cucumber.api.TypeRegistry;
import cucumber.api.datatable.DataTableType;
import cucumber.api.datatable.TableRowTransformer;
import cucumber.runtime.java.test.CPH.Consumption;
import cucumber.runtime.java.test.Stepdefs.Person;

import java.util.Map;

import static java.util.Locale.ENGLISH;

public class ParameterTypes implements Configuration {

    @Override
    public TypeRegistry createTypeRegistry() {
        final TypeRegistry parameterTypeRegistry = new TypeRegistry(ENGLISH);

        parameterTypeRegistry.defineDataTableType(new DataTableType(
            "consumption",
            Consumption.class,
            new TableRowTransformer<Consumption>() {
                @Override
                public Consumption transform(Map<String, String> map) {
                    Consumption consumption = new Consumption();
                    consumption.drink = map.get("drink");
                    consumption.when = Integer.valueOf(map.get("when"));
                    return consumption;
                }
            }));

        parameterTypeRegistry.defineDataTableType(new DataTableType(
            "person",
            Person.class,
            new TableRowTransformer<Person>() {
                @Override
                public Person transform(Map<String, String> map) {
                    Person person = new Person();
                    person.first = map.get("first");
                    person.last = map.get("last");
                    return person;
                }
            }));


        return parameterTypeRegistry;
    }
}
