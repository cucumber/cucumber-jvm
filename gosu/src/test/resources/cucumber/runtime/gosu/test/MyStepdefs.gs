package cucumber.runtime.gosu.test

uses cucumber.runtime.gosu.GosuBackend
uses cucumber.runtime.gosu.Stepdefs

class MyStepdefs implements Stepdefs {
    function define(backend: GosuBackend) {

        // TODO: Instead of defining a class it would be much nicer to define stepdefs
        // in a "script". Is that a .gsp? Can we have several of those?

        backend.Given("I have (\\d+) cukes in the belly", \ s : String -> {
            print("<contents>" + s + "</contents>")
            print("<hi>" + s + "</hi>")
        })
    }
}
