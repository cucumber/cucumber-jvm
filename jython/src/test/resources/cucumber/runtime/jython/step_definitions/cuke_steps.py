@Given('I have (\d+) "(.+)" in my belly')
def something_in_the_belly(self, n, what):
  self.n = int(n)
  self.what = what

@Then('^I am "([^"]*)"$')
def i_am(self, mood):
  if ("happy" != mood):
    raise(Exception("Not happy, only %d %s" % (self.n, self.what)))

@Before()
def default_cukes_in_the_belly(self):
  self.n = 5
  self.what = "cukes"

@Before('@must_have_more_cukes','@and_then_some')
def more_cukes_in_the_belly(self):
  self.n = self.n + 1
  self.what = "cukes"

@Given('^I have "([^"]*)" cukes in my belly$')
def I_have_cukes_in_my_belly(self, arg1):
  val = int(arg1)
  if (self.n != val):
    raise(Exception("Default cukes were %d, not %d" % (self.n, val)))


