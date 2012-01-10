scriptDir = new File(getClass().protectionDomain.codeSource.location.path).getParent();
this.class.classLoader.rootLoader.addURL(new File(scriptDir, "cucumber-groovy-full.jar").toURL())
this.class.classLoader.loadClass("cucumber.cli.Main").main(args)