require 'cuke4duke/spring'

spring_config('context.xml', 'steps.xml')

# Why is this needed? Isn't steps.xml supposed to register steps?
register_class(Java::simple.WorldSteps)
