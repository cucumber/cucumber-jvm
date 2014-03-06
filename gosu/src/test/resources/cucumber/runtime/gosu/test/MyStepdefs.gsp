// TODO: This should be defined in dsl.gsp, but I haven't figured out how
// to define global functions. It would also be ok to define a global
// object and make this a method on that object. It would then be invoked
// as glue.Given(...)
//
function Given(regexp: String, body(String) : void) : void {
    // TODO: Create a stepdef object and give it back to the GosuBackend
    // so it can be invoked later.
    // For now we'll just execute it immediately to play around...
    print("Stepdef:" + regexp);
    body("42");
}

Given("I have (\\d+) cukes in my belly", \ cukes -> {
    print("Nom nom, ${cukes} cukes");
})
