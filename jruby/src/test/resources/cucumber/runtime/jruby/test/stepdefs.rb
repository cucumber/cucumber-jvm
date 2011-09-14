Given /I have (\d+) "(.?*)" in my belly/ do |n, what|
  @n = n.to_i
  @what = what
end

Then /^I am "([^"]*)"$/ do |mood|
  if ("happy" != mood || @n < 4)
    raise("Not happy, only #{@n} #{@what}")
  end
end

Given /^Something( with an optional argument)?$/ do |argument|
  @argument = argument
end

Then /^the argument should be nil/ do
  if (!@argument.nil?)
    raise("Argument should be nil")
  end
end

Then /^the argument should not be nil/ do
  if (@argument.nil?)
    raise("Argument should not be nil")
  end
end

