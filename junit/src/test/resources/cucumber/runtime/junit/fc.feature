Feature: FC
# Scenario outline feature

  Scenario Outline:
    When foo <x>
    Then bar <y>

	Examples:
	| x | y   |
	| 1 | one |
	| 2 | two |
	
	Examples:
	| x | y     |
	| 3 | three |
