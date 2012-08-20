#!/usr/bin/env jython
import sys, inspect, os

cucumber_jython_path = os.path.dirname(inspect.getfile(inspect.currentframe())) + "/cucumber-jython.jar"
sys.path.append(cucumber_jython_path)

from java.io import File
from java.net import URLClassLoader
from cucumber.cli import Main
from cucumber.runtime import Runtime
from cucumber.runtime.jython import JythonBackend

cl = URLClassLoader([File(cucumber_jython_path).toURL()], Main.getClassLoader())

def createRuntime(resourceLoader, gluePaths, classLoader, dryRun):
    # TODO - pass in current jython runtime - PythonInterpreter
    jythonBackend = JythonBackend(resourceLoader)
    return Runtime(resourceLoader, gluePaths, classLoader, [jythonBackend], dryRun)

Main.run(sys.argv[1:], cl)
