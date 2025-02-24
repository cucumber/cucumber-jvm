Feature: Hooks - Named
  Hooks are special steps that run before or after each scenario's steps.

  Hooks can be given a name. Which is nice for reporting. Otherwise they work
  exactly the same as regular hooks.

  Scenario: With a named before and after hook
    When a step passes
