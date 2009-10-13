require 'cucumber/jvm_support/backtrace_filter'
Cucumber::Ast::StepInvocation::BACKTRACE_FILTER_PATTERNS << /org\/mozilla\/javascript/