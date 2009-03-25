module Cucumber #:nodoc:
  module Java #:nodoc:
    # IMPORTANT - KEEP IN SYNC WITH pom.xml version and Manifest.txt
    class VERSION #:nodoc:
      MAJOR = 0
      MINOR = 0
      TINY  = 1
      PATCH = nil # Set to nil for official release

      STRING = [MAJOR, MINOR, TINY, PATCH].compact.join('.')
    end
  end
end
