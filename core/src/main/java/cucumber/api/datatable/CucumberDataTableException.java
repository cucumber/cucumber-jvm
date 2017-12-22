package cucumber.api.datatable;

class CucumberDataTableException extends RuntimeException {
    CucumberDataTableException(String message) {
        super(message);
    }

    CucumberDataTableException(Throwable e) {
        super(e);
    }
}
