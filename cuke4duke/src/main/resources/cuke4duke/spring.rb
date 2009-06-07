require 'cuke4duke/java'
self.step_mother = ::Java::Cuke4dukeInternal::SpringStepMother.new

def spring_config(*configs)
  step_mother.setConfigs(configs)
end
