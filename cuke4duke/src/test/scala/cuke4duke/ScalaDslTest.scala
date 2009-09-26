package cuke4duke

import internal.JRuby
import internal.language.StepDefinition
import internal.scala.ScalaStepDefinition

import org.jruby.exceptions.RaiseException
import org.junit.{Test, Before => JunitBefore, Assert}
import Assert._

import _root_.scala.collection.mutable.Map
import org.jruby.RubyArray

class ScalaDslTest extends ScalaDsl with Norwegian {

  var result = ""

  val step = Map[String, StepDefinition]()

  def regexp(step:ScalaStepDefinition) = step.regexp_source

  def rubyClassName(raise:RaiseException) = raise.getException.getType.getName

  def array(s:String*) = {
    val r = RubyArray.newArray(JRuby.getRuntime)
    s.foreach(r.add(_))
    r
  }

  @JunitBefore
  def setUp{
    JRuby.getRuntime().evalScriptlet("""
    module Cucumber
      class Pending < StandardError; end
      class Undefined < StandardError; end
      class ArityMismatchError < StandardError; end 
    end
    """);

    for(s <- stepDefinitions)
      step(regexp(s)) = s
  }

  Before{
    result = "before hook 0"
  }

  @Test
  def test_before{
    val hook = beforeHooks(0)
    assertEquals(0, hook.tag_names.size)
    hook.invoke("", null)
    assertEquals("before hook 0", result)
  }


  Before("tag1", "tag2"){
    result = "tagged before"
  }

  @Test
  def test_taggedBefore{
    val hook = beforeHooks(1)
    assertEquals(2, hook.tag_names.size)
    assertEquals("tag1", hook.tag_names.get(0))
    assertEquals("tag2", hook.tag_names.get(1))
    hook.invoke("", null)
    assertEquals("tagged before", result)
  }

  After{
    result = "after hook 0"
  }

  @Test
  def test_after{
    val hook = afterHooks(0)
    assertEquals(0, hook.tag_names.size)
    hook.invoke("", null)
    assertEquals("after hook 0", result)
  }


  After("tag1", "tag2"){
    result = "tagged after"
  }

  @Test
  def test_taggedAfter{
    val hook = afterHooks(1)
    assertEquals(2, hook.tag_names.size)
    assertEquals("tag1", hook.tag_names.get(0))
    assertEquals("tag2", hook.tag_names.get(1))
    hook.invoke("", null)
    assertEquals("tagged after", result)
  }


  Given("call by name"){
    result = "given call-by-name"
  }

  @Test
  def test_CallByName{
    step("call by name").invoke(array())
    assertEquals("given call-by-name", result)
  }


  Given("given f1") { s: String =>
    result = "given f1 "+s
  }

  @Test
  def test_GivenF1{
    step("given f1").invoke(array("xxx"))
    assertEquals("given f1 xxx", result)
  }


  Given("pending comment"){
    pending("comment")
  }

  @Test
  def test_Pending{
    val comment = try{
      step("pending comment").invoke(array())
      fail("did not throw Cucumber::Pending")
    } catch {
      case e:RaiseException if rubyClassName(e) == "Cucumber::Pending" => e.getMessage
    }
    assertEquals("comment", comment)
  }

  When("pending no comment"){
    pending
  }

  @Test
  def test_PendingNoComment{
    val comment = try{
      step("pending no comment").invoke(array())
      fail("did not throw Cucumber::Pending")
    } catch {
      case e:RaiseException if rubyClassName(e) == "Cucumber::Pending" => e.getMessage
    }
    assertEquals("TODO", comment)
  }

  When("when f2 -> unit"){ (s: String, i: Int) =>
    result = "when f2 "+s + " " + i
  }

  @Test
  def test_WhenF2{
    step("when f2 -> unit").invoke(array("foo", "5"))
    assertEquals("when f2 foo 5", result)
  }

  Then("then f3 -> int"){ (s: String, i: Int, b: Boolean) =>
    result = "then f3 "+s+" "+i+" "+b
    5 // just to demonstrate that non-call-by-name can end in a non-unit expression
  }

  @Test
  def test_ThenF3{
    step("then f3 -> int").invoke(array("foo", "5", "true"))
    assertEquals("then f3 foo 5 true", result)
  }

  @Test
  def test_ArityMismatchException{
    try{
      step("then f3 -> int").invoke(array("foo", "5")) //missing the third argument
      fail("did not throw Cucumber::ArityMismatchError")
    } catch {
      case e:RaiseException if rubyClassName(e) == "Cucumber::ArityMismatchError" => e.getMessage
    }
  }

  Given("unknown type"){ a:(String,String) =>
    
  }

  @Test
  def test_Undefined{
    try{
      step("unknown type").invoke(array("foo"))
      fail("did not throw Cucumber::Undefined")
    } catch {
      case e:RaiseException if rubyClassName(e) == "Cucumber::Undefined" => e.getMessage
    }
  }

  class User(name:String){
    def friends_with(user:User):Boolean = name match {
      case "a" => true
      case "b" => false
    }
  }
  object User{
    def find_by_username(name:String):Option[User] = Some(new User(name))
  }

  Transform[User](name => User.find_by_username(name))

  var user:User = _
  
  Then("""^(\w+) should be friends with (\w+)$"""){ (user:User, friend:User) =>
    assertTrue(user.friends_with(friend))
  }
  
  @Test
  def test_transform{
    val friends = step("""^(\w+) should be friends with (\w+)$""")
    friends.invoke(array("a", "b"))
    try{
      friends.invoke(array("b", "a"))
      fail("b should not be friends with a")
    } catch {
      case e:AssertionError =>
    }
  }

  Given("g file_colon_line"){}
  When("w file_colon_line"){ a:String => ()}
  Then("t file_colon_line"){ (a:User, b:String) => ()}

  @Test
  def test_file_colon_line{
    assertEquals("Given(\"g file_colon_line\"){ () => ... }", step("g file_colon_line").file_colon_line)
    assertEquals("When(\"w file_colon_line\"){ String => ... }", step("w file_colon_line").file_colon_line)
    assertEquals("Then(\"t file_colon_line\"){ (User,String) => ... }", step("t file_colon_line").file_colon_line)
  }

  Gitt("Gitt i18n"){}
  Når("Når i18n"){ a:String => ()}
  Så("Så i18n"){ (a:User, b:String) => ()}

  @Test
  def test_file_colon_line_i18n{
    assertEquals("Gitt(\"Gitt i18n\"){ () => ... }", step("Gitt i18n").file_colon_line)
    assertEquals("Når(\"Når i18n\"){ String => ... }", step("Når i18n").file_colon_line)
    assertEquals("Så(\"Så i18n\"){ (User,String) => ... }", step("Så i18n").file_colon_line)
  }

  //known limitation: call by name steps are defined 'f: => Unit' which gets converted to a 'f() => Unit'
  //if it was defined 'f: => Any' it would swallow the other variants (f0..f22 => _) as we capture the whole function
  //as a value
  //if your last expression in a call by name step has a non-unit returntype there are two ways to fix it
  //
  //Implementing it as 'Given("returnvalue"){ ()=> 5 }'
  //or explicitly returning unit 'Given("returnvalue"){ 5; () }
  //
  //for the reasons given, this will not compile
  //Given("call by name -> !unit"){
  //  5
  //}
}