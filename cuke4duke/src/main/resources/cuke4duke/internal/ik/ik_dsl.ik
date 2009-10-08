  use("ispec")

import(java:util:ArrayList)

Cucumber = Origin mimic

Cucumber StepDefinition = Origin mimic do(
  initialize = method(regexp, code,
    @regexp = regexp
    args = @regexp names map(m, Message fromText(m asText))
    args << code
    @lexicalBlock = LexicalBlock createFrom(args, Ground)
    self
  )

  arguments_from = method(stepName,
    if(@regexp =~ stepName,
      args = ArrayList new
      @captures = it captures
      it captures each(n, c,
        args add(cuke4duke:internal:language:StepArgument new(c, it start(n+1)))
      )
      args,
      nil
    )
  )

  invoke = method(
    @lexicalBlock call(*(@captures))
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
