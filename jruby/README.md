## Using JRuby

To try out cucumber-jruby, first build the full jar. Change directory to the root level and run:

    ant jarjar

Then, cd to jruby and run the features:

    cd jruby
    jruby bin/cucumber-jvm --glue src/test/resources src/test/resources
