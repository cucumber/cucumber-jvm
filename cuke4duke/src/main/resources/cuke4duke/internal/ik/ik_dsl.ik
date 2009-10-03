import(java:util:ArrayList)

Cucumber = Origin mimic

Cucumber StepDefinition = Origin mimic do(
  create = method(regexp, code,
    @regexp = regexp
    @code = code
    self
  )

  arguments_from = method(stepName,
    if(@regexp =~ stepName,
      args = ArrayList new
      arg = cuke4duke:internal:language:StepArgument new("hello", 7)
      args add(arg)
      args,
      nil
    )
  )
)


Cucumber addStepDefinition = dmacro(
    [>regexp, code]
    CucumberLanguage addIokeStepDefinition(Cucumber StepDefinition create(regexp, code))
  )

Cucumber Given = Cucumber cell(:addStepDefinition)
Cucumber When  = Cucumber cell(:addStepDefinition)
Cucumber Then  = Cucumber cell(:addStepDefinition)
Ground mimic!(Cucumber)

; r = #/regexp ({named}.)/
; 
; if(r =~ "regexp 1",
;   it[:named]
;   it named
;   )
