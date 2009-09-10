require 'celerity'
require 'spec'

Given /^I am on the index page$/ do
  @browser = Celerity::Browser.new
  @browser.goto('http://localhost:8080/')
end

Then /^I should see "([^\"]*)"$/ do |txt|
  @browser.html.should =~ /#{txt}/m
end

Then /^I should see a link to "([^\"]*)"$/ do |url|
  @browser.html.should =~ /#{url}/m
end
