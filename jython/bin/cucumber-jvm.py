#!/usr/bin/env jython
import sys, inspect, os

cucumber_jython_shaded_path = os.path.dirname(inspect.getfile(inspect.currentframe())) + "/cucumber-jython-shaded.jar"
sys.path.append(cucumber_jython_shaded_path)

from java.io import File
from java.lang import Thread
from java.net import URLClassLoader
from cucumber.api.cli import Main

cl = URLClassLoader([File(cucumber_jython_shaded_path).toURL()], Main.getClassLoader())
Thread.currentThread().contextClassLoader = cl

exitstatus = Main.run(sys.argv[1:], cl)
sys.exit(exitstatus)
