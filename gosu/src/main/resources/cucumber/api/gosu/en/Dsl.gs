package cucumber.api.gosu.en

uses cucumber.runtime.gosu.GosuBackend

class Dsl {
    static var backend : GosuBackend = GosuBackend.instance

    static function Given( regexp : String, body(p1:String) : void ) : void {
        backend.addStepDefinition(regexp, body)
    }

    static function When( regexp : String, body(p1:String) : void ) : void {
        backend.addStepDefinition(regexp, body)
    }

    static function Then( regexp : String, body(p1:String) : void ) : void {
        backend.addStepDefinition(regexp, body)
    }
}