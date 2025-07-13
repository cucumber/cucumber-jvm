# DataTable Matchers

Contains [Hamcrest matchers](http://hamcrest.org/) to compare data tables.
These can be used in most common test frameworks and produces pretty error
messages.

Add the `datatable-matchers` dependency to your `pom.xml`
and use the [`cucumber-bom`](../cucumber-bom/README.md) for dependency management:

```
<dependencies>
  [...]
    <dependency>
        <groupId>io.cucumber</groupId>
        <artifactId>datatable-matchers</artifactId>
        <scope>test</scope>
    </dependency>
  [...]
</dependencies>
```

Use the matcher in your step definition.

```java
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Then;

import static io.cucumber.datatable.matchers.DataTableHasTheSameRowsAs.hasTheSameRowsAs;
import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;

public class StepDefinitions {

    // Provided by the system under test
    DataTable actual = DataTable.create(asList(
            asList("Annie M. G.", "Schmidt"),
            asList("Roald", "Dahl")
    ));

    @Then("these authors have registered:")
    public void these_authors_have_registered(DataTable expected) {
        assertThat(actual, hasTheSameRowsAs(expected).inOrder());
        // java.lang.AssertionError: 
        // Expected: a datable with the same rows
        //     but: the tables were different
        //    + | Annie M. G. | Schmidt  |
        //      | Roald       | Dahl     |
        //    - | Astrid      | Lindgren |
    }
}
```

