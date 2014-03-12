uses cucumber.api.gosu.en.Dsl

Dsl.Then('there are "(\\d+)" cukes in my belly', \ cukes : java.lang.Integer -> {
    print("I have ${cukes} cukes");
})
