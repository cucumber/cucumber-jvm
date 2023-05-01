package io.cucumber.spring;

import org.apiguardian.api.API;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation is used on a configuration class to make the Cucumber aware
 * of the test configuration. This is to be used in conjunction with
 * {@code @ContextConfiguration}, {@code @ContextHierarchy} or
 * {@code @BootstrapWith}. In case of SpringBoot, the configuration class can be
 * annotated as follows:
 * <p />
 *
 * <pre class="code">
 * &#064;CucumberContextConfiguration
 * &#064;SpringBootTest(classes = TestConfig.class)
 * public class CucumberSpringConfiguration {
 *
 * }
 * </pre>
 *
 * Note:
 * <ul>
 * <li>Only one glue class should be annotated with
 * {@code @CucumberContextConfiguration} otherwise an exception will be
 * thrown.</li>
 * <li>Cucumber Spring uses Spring's {@code TestContextManager} framework
 * internally. As a result a single Cucumber scenario will mostly behave like a
 * JUnit test.</li>
 * <li>Step definitions should not be annotated with {@code @Component}
 * annotation. Doing so will lead to an exception.</li>
 * </ul>
 * <h2>Accessing the components from application context</h2> Components in the
 * step definitions can be accessed by autowiring. A simple example is as
 * follows:
 * <p />
 * 
 * <pre class="code">
 * package com.example.app;
 * 
 * import org.springframework.beans.factory.annotation.Autowired;
 * import io.cucumber.java.en.Given;
 * 
 * public class MyStepDefinitions {
 * 
 *     &#064;Autowired
 *     private MyService myService;
 *
 *     &#064;Given("feed back is requested from my service")
 *     public void feed_back_is_requested() {
 *         myService.requestFeedBack();
 *     }
 * }
 * </pre>
 *
 * <h2>Configuring multiple Test Application Contexts</h2> Per execution
 * Cucumber can only launch a single Test Application Context. To use multiple
 * different application contexts Cucumber must be executed multiple times.
 * <p />
 * <h3>JUnit 4 / TestNG</h3>
 * 
 * <pre class="code">
 * package com.example;
 *
 * import io.cucumber.junit.Cucumber;
 * import io.cucumber.junit.CucumberOptions;
 * import org.junit.runner.RunWith;
 *
 * &#064;RunWith(Cucumber.class)
 * &#064;CucumberOptions(glue = "com.example.application.one",
 *         features = "classpath:com/example/application.one")
 * public class ApplicationOneTest {
 *
 * }
 * </pre>
 *
 * Repeat as needed.
 * <p />
 * <h3>JUnit 5 + JUnit Platform Suite</h3>
 *
 * <pre class="code">
 * package com.example;
 *
 * import org.junit.platform.suite.api.ConfigurationParameter;
 * import org.junit.platform.suite.api.SelectClasspathResource;
 * import org.junit.platform.suite.api.Suite;
 *
 * import static io.cucumber.junit.platform.engine.Constants.GLUE_PROPERTY_NAME;
 * 
 * &#064;Suite
 * &#064;SelectClasspathResource("com/example/application/one")
 * &#064;ConfigurationParameter(key = GLUE_PROPERTY_NAME,
 *         value = "com.example.application.one")
 * public class ApplicationOneTest {
 *
 * }
 * </pre>
 *
 * Repeat as needed.
 * <p />
 * <h2>Sharing State</h2> All scenarios as well as the tests (e.g. JUnit) that
 * use same context configuration will share one instance of the Spring
 * application. This avoids an expensive startup time.
 * <p />
 * <h3>Sharing state between steps</h3> To prevent sharing test state between
 * scenarios, beans containing glue code (i.e. step definitions, hooks, etc.)
 * are bound to the {@code cucumber-glue} scope. The {@code cucumber-glue} scope
 * starts prior to a scenario and ends after a scenario. All beans in this scope
 * will be created before a scenario execution and disposed at the end of it. By
 * using the {@code @ScenarioScope} annotation additional components can be
 * added to the glue scope. These components can be used to safely share state
 * between steps inside a scenario.
 * <p />
 * 
 * <pre class="code">
 * package com.example.app;
 *
 * import org.springframework.stereotype.Component;
 * import io.cucumber.spring.ScenarioScope;
 *
 * &#064;Component
 * &#064;ScenarioScope
 * public class TestUserInformation {
 *
 *     private User testUser;
 * 
 *     public void setTestUser(User testUser) {
 *         this.testUser = testUser;
 *     }
 * 
 *     public User getTestUser() {
 *         return testUser;
 *     }
 *
 * }
 * </pre>
 *
 * The glue scoped component can then be autowired into a step definition:
 * <p />
 * 
 * <pre class="code">
 * package com.example.app;
 *
 * import org.springframework.beans.factory.annotation.Autowired;
 * import io.cucumber.java.en.Given;
 *
 * public class UserStepDefinitions {
 *
 *    &#064;Autowired
 *    private UserService userService;
 *
 *    &#064;Autowired
 *    private TestUserInformation testUserInformation;
 *
 *    &#064;Given("there is a user")
 *    public void there_is_as_user() {
 *       User testUser = userService.createUser();
 *       testUserInformation.setTestUser(testUser);
 *    }
 * }
 *
 * public class PurchaseStepDefinitions {
 *
 *    &#064;Autowired
 *    private PurchaseService purchaseService;
 *
 *    &#064;Autowired
 *    private TestUserInformation testUserInformation;
 *
 *    &#064;When("the user makes a purchase")
 *    public void the_user_makes_a_purchase(){
 *       Order order = ....
 *       User user = testUserInformation.getTestUser();
 *       purchaseService.purchase(user, order);
 *    }
 * }
 * </pre>
 *
 * <h3>Sharing state between threads</h3> By default, when using
 * {@code @ScenarioScope}, these beans must also be accessed on the same thread
 * as the one that is executing the scenario. If you are certain that your
 * scenario scoped beans can only be accessed through step definitions you can
 * use {@code @ScenarioScope(proxyMode = ScopedProxyMode.NO)}.
 * <p />
 * 
 * <pre class="code">
 * package com.example.app;
 *
 * import org.springframework.stereotype.Component;
 * import io.cucumber.spring.ScenarioScope;
 * import org.springframework.context.annotation.ScopedProxyMode;
 *
 * &#064;Component
 * &#064;ScenarioScope(proxyMode = ScopedProxyMode.NO)
 * public class TestUserInformation {
 *
 *    private User testUser;
 *
 *    public void setTestUser(User testUser) {
 *       this.testUser = testUser;
 *    }
 *
 *    public User getTestUser() {
 *       return testUser;
 *    }
 * }
 *
 *
 * package com.example.app;
 *
 * import org.springframework.beans.factory.annotation.Autowired;
 * import io.cucumber.java.en.Given;
 * import org.awaitility.Awaitility;
 *
 * public class UserStepDefinitions {
 *
 *    &#064;Autowired
 *    private TestUserInformation testUserInformation;
 *
 *    &#064;Then("the test user is eventually created")
 *    public void a_user_is_eventually_created() {
 *       Awaitility.await()
 *           .untilAsserted(() -> {
 *               // This happens on a different thread
 *               TestUser testUser = testUserInformation.getTestUser();
 *               Optional&lt;user&gt; user = repository.findById(testUser.getId());
 *               assertTrue(user.isPresent());
 *           });
 *    }
 * }
 * </pre>
 * 
 * <h2>Dirtying the application context</h2> If the test modifies the
 * application context then {@code @DirtiesContext} can be added to the test
 * configuration to recreate the context for later tests.
 * <p />
 * 
 * <pre class="code">
 * package com.example.app;
 * 
 * import org.springframework.beans.factory.annotation.Autowired;
 * import org.springframework.test.annotation.DirtiesContext;
 * import org.springframework.boot.test.context.SpringBootTest;
 * 
 * import io.cucumber.spring.CucumberContextConfiguration;
 * 
 * &#064;CucumberContextConfiguration
 * &#064;SpringBootTest(classes = TestConfig.class)
 * &#064;DirtiesContext
 * public class CucumberSpringConfiguration {
 * 
 * }
 *
 *
 * package com.example.app;
 * 
 * public class MyStepDefinitions {
 * 
 *    &#064;Autowired
 *    private MyService myService;  // Each scenario have a new instance of MyService
 * 
 * }
 * </pre>
 * 
 * Note: Using {@code @DirtiesContext} in combination with parallel execution
 * will lead to undefined behaviour.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@API(status = API.Status.STABLE)
public @interface CucumberContextConfiguration {

}
