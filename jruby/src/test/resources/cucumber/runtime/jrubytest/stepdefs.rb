require 'cucumber/api/jruby/en'
require 'minitest'

class MinitestWorld
  include Minitest::Assertions
  attr_accessor :assertions

  def initialize
    self.assertions = 0
  end
end

World do
  MinitestWorld.new
end

Before do
  raise "There is a leak" if @before_var
  @before_var = 10
end

Given /I have (\d+) "(.?*)" in my belly/ do |n, what|
  assert_equal(10, @before_var)
  @n = n.to_i
  @what = what
end

Then /^I am "([^"]*)"$/ do |mood|
  assert_equal("happy", mood, "Should be happy, only #{mood}!")
  assert_equal(4, @n, "Need 4 cukes, only #{@n}!");
end

Given /^Something( with an optional argument)?$/ do |argument|
  @argument = argument
end

Then /^the argument should be nil/ do
  assert_nil(@argument, "Argument should be nil")
end

Then /^the argument should not be nil/ do
  refute_nil(@argument, "Argument should not be nil")
end

Given /^a pending stepdef without an explicit reason$/ do
  begin
    pending
  rescue Exception => @exception
  end
end

Given /^a pending stepdef with reason "([^"]*)"$/ do |reason|
  begin
    pending reason
  rescue Exception => @exception
  end
end

Then /^the pending stepdef throws a pending exception with "([^"]*)"$/ do |message|
  assert_match /#{message}$/, @exception.message
end

Given /^a step called from another$/ do
  @called ||= 0
  @called += 1
end

When /^I call that step$/ do
  step "a step called from another"
end

Then /^the step got called$/ do
  assert_equal(2, @called)
end

When /I call an undefined step from another$/ do
  begin
    step "HOLY MOLEYS THIS DOESN'T EXIST!"
  rescue Exception => e
    @exception = e
  end
end

Then /I get an exception with "([^"]*)"$/ do |message|
  assert_match /#{message}$/, @exception.message
  assert_equal(__FILE__, @exception.stackTrace[0].fileName)
  assert_equal(78, @exception.stackTrace[0].lineNumber)
end

Given /^a data table:$/ do |table|
  @hashes = build_hashes table.raw
end

When /^I call that data table from this step:$/ do |table|
  @old_hashes = @hashes
  step "a data table:", table
end

Then /^that data table step got called$/ do
  refute_equal(@old_hashes, @hashes)

  assert_equal("omg", @old_hashes[0]["field"])
  assert_equal("wtf", @old_hashes[0]["value"])

  assert_equal("omg", @hashes[0]["field"])
  assert_equal("lol", @hashes[0]["value"])
end

def build_hashes(raw)
  output = Array.new
  keys = Array.new
  raw.size.times do |x|
    if x == 0
      raw[x].each do |header|
        keys << header.to_s
      end
    else
      hash = Hash.new
      raw[x].size.times do |y|
        hash[keys[y]] = raw[x][y]
      end
      output << hash
    end
  end
  output
end

Given /^I store the value "([^"]*)"$/ do |value|
  @value = value
end

When /^I grab another value "([^"]*)"$/ do |value|
  @another_value = value
end

Then /^those values are the same$/ do
  assert_equal(@value, @another_value)
end
