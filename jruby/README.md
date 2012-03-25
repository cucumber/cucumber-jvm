## Using JRuby

Run the features:

    jruby bin/cucumber-jvm --glue src/test/resources src/test/resources

### cucumber-jruby.properties resource bundle

There is a resource bundle available to override a few environment variables for jruby.
Specifically, the ability to set the GEM_PATH and to specify whether to run jruby in 1.8 or 1.9 mode.

If the properties file is not present in the root of the classpath, any environment variables will operate
as intended, otherwise the properties file will override the environment. Makes it much easier to run the features
from within the IDE, and it encapsulates the settings in the project source tree easily.

Example cucumber-jruby.properties file

    GEM_PATH=${basedir}/src/test/gems

When filtered by the maven properties file, it will set Jruby's GEM_PATH to src/test/gems (expanded out to the full file
system) so that Jruby can find any Gems you might be using.

The constructor of `JRubyBackend.java` has the currently supported properties.

Handy!
