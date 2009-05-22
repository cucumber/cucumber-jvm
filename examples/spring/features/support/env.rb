require 'cucumber/spring'

spring_config('src/main/resources/context.xml', 'src/main/resources/steps.xml')

# Why is this needed? Isn't steps.xml supposed to register steps?
register_class(Java::steps.WorldSteps)
