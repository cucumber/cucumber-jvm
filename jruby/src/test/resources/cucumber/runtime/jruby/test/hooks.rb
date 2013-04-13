require 'cucumber/api/jruby/en'

Before('@tag') do
  @tagged_hook_ran = true
end

Then /^tagged hook ran$/ do
  raise "Tagged hook didn't run when it should" unless @tagged_hook_ran
end

Then /^tagged hook didn't run$/ do
  raise "Tagged hook ran when it shouldn't" if @tagged_hook_ran
end
