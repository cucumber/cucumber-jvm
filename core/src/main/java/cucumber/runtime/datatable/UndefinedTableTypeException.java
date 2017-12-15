package cucumber.runtime.datatable;


import cucumber.runtime.CucumberException;

public class UndefinedTableTypeException extends CucumberException {
    public UndefinedTableTypeException(String typeName) {
        super(String.format("Undefined table type {%s}", typeName));
    }
}
