@Given('I have (\d+) "(.+)" in my belly')
def something_in_the_belly(self, n, what):
  self.n = int(n)
  self.what = what

@Given('^I am "([^"]*)"$')
def i_am(self, mood):
  if ("happy" != mood):
    raise(Exception("Not happy, only %d %s" % (self.n, self.what)))
