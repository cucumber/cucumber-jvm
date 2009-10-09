Given(#/^I have some ({what}\w+) dudes:$/,
  what should == "fine"
  ; Either we have an 'table' implicit name for tables,
  ; Or we declare the step definition in such a way that we
  ; decide the table variable name ourself. Not sure how...
  firstDude = table raw get(1) get(0)
  firstDude asText should == "Ola"
)