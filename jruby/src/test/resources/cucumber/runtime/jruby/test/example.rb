# JRUBY will barf out warnings when we redefine constants :D
DEFINE_A_CONSTANT = 1

#this will help me ensure that jruby stuff is only loaded one time
# otherwise tests will bomb and fail, as they should.
if @loaded_already
  raise "OMG PARSED MULTIPLE TIMES!!!!1"
else
  @loaded_already = "LOADED AT #{Time.now}"
end
