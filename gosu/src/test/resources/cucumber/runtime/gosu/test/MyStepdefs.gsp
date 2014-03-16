uses cucumber.api.gosu.en.Dsl

Dsl.Given('I have "(\\d+)" cukes in my belly', \ cukes : String -> {
    print("Nom nom, ${cukes} cukes");
})
