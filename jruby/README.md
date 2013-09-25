## Using JRuby

Run the features:

```
jruby bin/cucumber-jvm --glue src/test/resources src/test/resources
```

### Customizing JRuby

You can define `GEM_PATH` to tell JRuby where to load gems from.
You can also define `RUBY_VERSION` to tell JRuby to run in `1.8`, `1.9` or `2.0` mode.
Consult the JRuby documentation to see what the default mode is.

These values can be defined in any of the three places:

* In an Environment variable
* In a System property (for example `-DRUBY_VERSION=1.9`
* In a `cucumber-jvm.properties` properties file on your `CLASSPATH`.

Sample cucumber-jvm.properties file:

```
GEM_PATH=${basedir}/src/test/gems
```
