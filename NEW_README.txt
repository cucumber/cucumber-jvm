Make sure you have this on your PATH:

~/.gem/jruby/1.8/bin
(This is where cuke4duke will install gems)

Then from Root:

mvn clean install -P examples -Dcucumber.installGems=true

TODO: 

* Use a GEM_PATH that will install gems underneath cucumber in maven
* Package ruby code inside (maven) jar?
* Package jar inside gem?
* Both package schemes??