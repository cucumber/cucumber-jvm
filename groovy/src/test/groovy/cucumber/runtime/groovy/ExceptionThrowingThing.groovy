package cucumber.runtime.groovy

class ExceptionThrowingThing {
    def methodMissing(String name, args) {
        throw new RuntimeException("Don't have method $name taking $args")
    }

    RuntimeException returnGroovyException() {
        try {
            this.foo()
            null
        } catch(RuntimeException e) {
            e
        }
    }
}
