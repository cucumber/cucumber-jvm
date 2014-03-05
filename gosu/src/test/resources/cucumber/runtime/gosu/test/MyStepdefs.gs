package cucumber.runtime.gosu.test

uses cucumber.runtime.gosu.GosuBackend
uses cucumber.runtime.gosu.Stepdefs

class MyStepdefs implements Stepdefs {
    function define(backend: GosuBackend) {
        backend.Given("I have (\\d+) cukes in the belly", \ s : String -> {
            print("<contents>" + s + "</contents>")
            print("<hi>" + s + "</hi>")
        })
    }
}
