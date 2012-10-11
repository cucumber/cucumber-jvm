scriptDir = new File(getClass().protectionDomain.codeSource.location.path).getParent();
if (this.class.classLoader.rootLoader) this.class.classLoader.rootLoader.addURL(new File(scriptDir, "cucumber-groovy.jar").toURL())
this.class.classLoader.loadClass("cucumber.api.cli.Main").main(args)