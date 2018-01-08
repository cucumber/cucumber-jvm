package io.cucumber.stepexpression;


import cucumber.runtime.CucumberException;

class UndefinedTableTypeException extends CucumberException {
    UndefinedTableTypeException(String typeName) {
        super(String.format("Undefined table type {%s}", typeName));
    }
}
