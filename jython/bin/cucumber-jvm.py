#!/usr/bin/env jython
import sys, inspect, os

cucumber_jython_shaded_path = os.path.dirname(inspect.getfile(inspect.currentframe())) + "/cucumber-jython-shaded.jar"
sys.path.append(cucumber_jython_shaded_path)

from java.io import File
from java.net import URLClassLoader
from cucumber.api.cli import Main
from cucumber.runtime import Runtime
from cucumber.runtime.jython import JythonBackend

cl = URLClassLoader([File(cucumber_jython_shaded_path).toURL()], Main.getClassLoader())

Main.run(sys.argv[1:], cl)
