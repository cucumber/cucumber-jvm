Given /I have (\d+) "(.?*)" in my belly/ do |n, what|
  @n = n.to_i
  @what = what
end

Then /^I am "([^"]*)"$/ do |mood|
  if ("happy" != mood || @n < 4) 
    raise("Not happy, only #{@n} #{@what}")
  end
end
