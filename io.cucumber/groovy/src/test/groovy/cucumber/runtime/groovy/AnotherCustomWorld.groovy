package cucumber.runtime.groovy

class AnotherCustomWorld {
    def aProperty
    def methodArgs

    def aMethod() {
        methodArgs = "no args"
    }

    def aMethod(List<Integer> args) {
        methodArgs = args
    }

    def getPropertyValue() {
        aProperty
    }


}
