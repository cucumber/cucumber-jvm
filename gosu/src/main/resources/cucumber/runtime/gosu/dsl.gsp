// This file defines the DSL for Gosu glue (step definitions and hooks).
// TODO: Figure out how to define global functions, or find some other workaround.

function Given(regexp: String, body(String) : void) : void {
    // TODO: Create a stepdef object and give it back to the GosuBackend
    print("Stepdef:" + regexp);
}

// The user's glue follows below...

