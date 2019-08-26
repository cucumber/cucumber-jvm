require 'cucumber/api/jruby/en'

Before('@tag') do |scenario|
  @tagged_hook_ran = true
  raise "Unexpected tags" unless scenario.source_tag_names == ['@tag']
end

Then /^tagged hook ran$/ do
  raise "Tagged hook didn't run when it should" unless @tagged_hook_ran
end

Then /^tagged hook didn't run$/ do
  raise "Tagged hook ran when it shouldn't" if @tagged_hook_ran
end
