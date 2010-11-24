use("ispec")

import(java:util:ArrayList)

Cucumber = Origin mimic

Cucumber StepDefinition = Origin mimic do(
  initialize = method(regexp, code, multilineArgName 'table,  ; multiline arg may ba a String or a Table, but Table is more common
    @regexp = regexp
    @code = code
    @multilineArgName = multilineArgName
    @arg_names = @regexp names map(m, Message fromText(m asText))
    self
  )

  regexp_source = method(
    @regexp inspect
  )

  arguments_from = method(stepName,
    if(@regexp =~ stepName,
      args = ArrayList new
      @arg_values = it captures
      it captures each(n, c,
        args add(gherkin:formatter:Argument new(it start(n+1), c))
      )
      args,
      nil
    )
  )

  invoke = method(multilineArg,
    arg_names = @arg_names mimic
    arg_values = @arg_values mimic
    if(multilineArg,
      arg_names << @multilineArgName
      arg_values << multilineArg
    )
    arg_names << @code
    lexicalBlock = LexicalBlock createFrom(arg_names, Ground)
    lexicalBlock call(*(arg_values))
  )
)

Cucumber addStepDefinition = dmacro(
    [>regexp, code]
    IokeBackend addStepDefinition(Cucumber StepDefinition mimic(regexp, code)),

    [>regexp, tableName, code]
    IokeBackend addStepDefinition(Cucumber StepDefinition mimic(regexp, code, tableName))
  )

Cucumber Pending = Condition mimic
Cucumber Given = Cucumber cell(:addStepDefinition)
Cucumber When  = Cucumber cell(:addStepDefinition)
Cucumber Then  = Cucumber cell(:addStepDefinition)
Cucumber pending = method(
  signal!(Cucumber Pending)
)

Ground mimic!(Cucumber)
