Feature: Hooks - Conditional execution
  Hooks are special steps that run before or after each scenario's steps.

  They can also conditionally target specific scenarios, using tag expressions

  @fail-before
  Scenario: A failure in the before hook and a skipped step
    When a step passes

  @fail-after
  Scenario: A failure in the after hook and a passed step
    When a step passes

  @passing-hook
  Scenario: With an tag, a passed step and hook
    When a step passes
