# DataTable Matchers

Contains [Hamcrest matchers](http://hamcrest.org/) to compare data tables.
These can be used in most common test frameworks and produces pretty error
messages.

Add the `datatable-matchers` dependency to your pom.

```
<dependencies>
  [...]
    <dependency>
        <groupId>io.cucumber</groupId>
        <artifactId>datatable-matchers</artifactId>
        <version>${cucumber-datatable.version}</version>
        <scope>test</scope>
    </dependency>
  [...]
</dependencies>
```

Use the matcher in your step definition.

```java
import static io.cucumber.datatable.matchers.DataTableHasTheSameRowsAs.hasTheSameRowsAs;

[...]

private final DataTable expected = DataTable.create(
    asList(
        asList("Annie M. G.", "Schmidt"),
        asList("Roald", "Dahl"),
));
    
@Then("these authors have registered:")
public void these_authors_have_registered(DataTable registeredAuthors){
    assertThat(registeredAuthors, hasTheSameRowsAs(expected).inOrder());
    
    // java.lang.AssertionError: 
    // Expected: a datable with the same rows
    // but: the tables were different
    // - | Annie M. G. | Schmidt  |
    //   | Roald       | Dahl     |
    // + | Astrid      | Lindgren |
} 
```

