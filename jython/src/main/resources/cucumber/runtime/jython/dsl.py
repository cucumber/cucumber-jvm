import re
from gherkin.formatter import Argument

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

class World:
  """The World"""

