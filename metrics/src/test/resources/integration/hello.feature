@hello 
Feature: hello  (Function to validate the environment.) 

	Scenario Outline: Function to validate the environment.
		Given me a hello, please. Best Regards '<author>'.
		
	Examples:
	#Begin Examples#
	|author|
	|Jenkins A|
	|Jenkins B|
	#End Examples#