import(java:util:ArrayList)

Cucumber = Origin mimic

Cucumber StepDefinition = Origin mimic do(
  initialize = method(regexp, code,
    @regexp = regexp
    @code = code
    self
  )

  arguments_from = method(stepName,
    if(@regexp =~ stepName,
      args = ArrayList new
      n = 1
      it captures each(c,
        args add(cuke4duke:internal:language:StepArgument new(c, it start(n)))
        n++
      )
      args,
      nil
    )
  )
  
  invoke = method(
    ; TODO - invoke @code. Either pass the regexp match from above as single arg,
    ; or do something magic to make it possible to access the args without using "it".
  )
  
)


Cucumber addStepDefinition = dmacro(
    [>regexp, code]
    CucumberLanguage addIokeStepDefinition(Cucumber StepDefinition mimic(regexp, code))
  )

Cucumber Given = Cucumber cell(:addStepDefinition)
Cucumber When  = Cucumber cell(:addStepDefinition)
Cucumber Then  = Cucumber cell(:addStepDefinition)
Ground mimic!(Cucumber)
