#!/usr/bin/env jython
import sys, inspect, os

sys.path.append(os.path.dirname(inspect.getfile(inspect.currentframe())) + "/cucumber-jython-full.jar")
from cucumber.cli import Main
# FIXME: This causes:
# cucumber.runtime.CucumberException: cucumber.runtime.CucumberException: No backends were found. Please make sure you have a backend module on your CLASSPATH.
# Most likely a CLASSPATH problem
# Main.main(sys.argv)