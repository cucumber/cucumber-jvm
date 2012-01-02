require 'test/unit'
include Test::Unit::Assertions

Given /I have (\d+) "(.?*)" in my belly/ do |n, what|
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
  assert_not_nil(@argument, "Argument should not be nil")
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
  assert_match /.*PendingException: #{message}$/, @exception.message
end
