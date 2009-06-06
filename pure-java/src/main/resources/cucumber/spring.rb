require 'cucumber/java'
self.step_mother = ::Java::CucumberInternal::SpringStepMother.new

def spring_config(*configs)
  step_mother.setConfigs(configs)
end
