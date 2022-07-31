/**
 * Cucumber Guice configuration Api
 * <p>
 * An implementation of this interface is used to obtain an
 * <code>com.google.inject.Injector</code> that is used to provide instances of
 * all the classes that are used to run the Cucumber tests. The injector should
 * be configured with a binding for <code>ScenarioScope</code>.
 * <p>
 * This module allows you to use Google Guice dependency injection in your
 * Cucumber tests. Guice comes as standard with singleton scope and 'no scope'.
 * This module adds Cucumber scenario scope to the scopes available for use in
 * your test code. The rest of this documentation assumes you have at least a
 * basic understanding of Guice. Please refer to the Guice wiki if necessary,
 * see
 * <a href="https://github.com/google/guice/wiki/Motivation" target="_parent">
 * https://github.com/google/guice/wiki/Motivation</a>
 * </p>
 * <p>
 * By including the <code>cucumber-guice</code> jar on your
 * <code>CLASSPATH</code> your Step Definitions will be instantiated by Guice.
 * There are two main modes of using the module: with scope annotations and with
 * module bindings. The two modes can also be mixed. When mixing modes it is
 * important to realise that binding a class in a scope in a module takes
 * precedence if the same class is also bound using a scope annotation.
 * </p>
 */
package io.cucumber.guice;
