Feature: Converters - issue-306: conversion fails when plain steps register converters first

	As a happy cucumber-jvm user
	I want to mix and match xstream converters in plain steps and in data tables
	So that I can use the same framework in both cases
	
	Scenario: some foo
		Given I have some foo named "MyFoo"

	Scenario: some bar
		Given I have some bar named "MyBar"

	Scenario: some baz
		Given I have some baz named "MyBaz"
		
	Scenario: do some stuff with data table first
		Given I have some stuff in a data table:
		  	| foo 		| bar		| baz		|
		  	| MyFoo2	| MyBar2	| MyBaz2	|

		  	