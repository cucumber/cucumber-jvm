require 'spec/expectations'
require 'celerity'

browser = Celerity::Browser.new

Before do
  @browser = browser
end

at_exit do
  browser.close
end
