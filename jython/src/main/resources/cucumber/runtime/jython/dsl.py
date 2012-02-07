import re
from gherkin.formatter import Argument
class I18NKeywordTemplate(object):
  def __init__(self, regexp):
    self.regexp = regexp
    
  def __call__(self, func):
    arity = func.func_code.co_argcount - 1
    backend.registerStepdef(StepDefinition(self.regexp, func), arity)
    return func

class StepDefinition:
  def __init__(self, regexp, func):
    self.regexp = regexp
    self.func = func

  def matched_arguments(self, step_name):
    match = re.match(self.regexp, step_name)
    if(match):
      n = 1
      arguments = []
      for val in match.groups():
        start = match.start(n)
        arguments.append(Argument(start, val))
        n += 1
      return arguments
    else:
      return None
    end

  def execute(self, *args):
    self.func.__call__(*args)

  def pattern(self):
    return self.regexp

And = But = Given = Then = When = I18NKeywordTemplate

class World:
  """The World"""
