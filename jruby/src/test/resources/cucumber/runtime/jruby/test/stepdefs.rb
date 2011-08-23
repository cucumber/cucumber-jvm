Given /I have (\d+) "(.?*)" in my belly/ do |n, what|
  @what = what
  @n = n.to_i
end

Then /^I am "([^"]*)"$/ do |mood|
  if ("happy" != mood || @n < 4) 
    raise("Not happy, only #{@n} #{@what}")
  end
end
