package cucumber.api.datatable;

class CucumberDataTableException extends RuntimeException {
    CucumberDataTableException(String message) {
        super(message);
    }

    CucumberDataTableException(String s, Throwable throwable) {
        super(s, throwable);
    }
}
