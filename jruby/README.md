## Using JRuby

To try out cucumber-jruby, first build the full jar. Change directory to the root level and run:

    mvn clean install -P full

Then, cd to jruby and run a feature:

    cd jruby
    java -classpath src/test/resources:target/cucumber-jruby-1.0.0-SNAPSHOT-full.jar cucumber.cli.Main --glue cucumber/runtime/jruby/test cucumber/runtime/jruby/test/cukes.feature

This is obviously a little convoluted, so we'll try to improve it so you can run:

    java -jar cucumber-jruby-1.0.0-SNAPSHOT-full.jar --glue src/test/resources src/test/resources/cucumber/runtime/jruby/test/cukes.feature
 
 Or even better:
    
    gem install cucumber-jvm
    cucumber-jvm src/test/resources/cucumber/runtime/jruby/test/cukes.feature