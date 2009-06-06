require 'cucumber/spring'

spring_config('context.xml', 'steps.xml')

# Why is this needed? Isn't steps.xml supposed to register steps?
register_class(Java::steps.WorldSteps)
