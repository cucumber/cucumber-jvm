uses cucumber.api.gosu.en.Dsl

Dsl.Given('I have "([^"]*)" cukes in my belly', \ cukes : String -> {
    print("Nom nom, ${cukes} cukes");
})
