## Using JRuby

To try out cucumber-jruby, first build the full jar. Change directory to the root level and run:

    mvn clean install -P full

Then, cd to jruby and run a feature:

    cd jruby
    java -classpath src/test/resources:target/cucumber-jruby-1.0.0.RC2-full.jar cucumber.cli.Main --glue cucumber/runtime/jruby/test cucumber/runtime/jruby/test/cukes.feature

This is obviously a little convoluted, so we'll try to improve it so you can run:

    java -jar target/cucumber-jruby-1.0.0.RC2-full.jar --glue src/test/resources src/test/resources/cucumber/runtime/jruby/test/cukes.feature
 
Or even better:
    
    mvn -Prelease-sign-artifacts clean
    #gem build cucumber-jvm.gemspec
    #gem install cucumber-jvm
    #cucumber-jvm --glue src/test/resources src/test/resources/cucumber/runtime/jruby/test/cukes.feature
    jruby bin/cucumber-jvm --glue src/test/resources src/test/resources/cucumber/runtime/jruby/test/cukes.feature

TODO: Why are we getting:

    class cucumber.io.FileResourceIterator$1 src/test/resources/cucumber/runtime/jruby/test/stepdefs.rb
    class cucumber.io.ClasspathIterator$2 src/test/resources/cucumber/runtime/jruby/test/stepdefs.rb <---- ?????????
    might be related to buggy resource loading.


TODO: Neuter the Test::Unit/Minitest runner like we do in RSpec/Cucumber