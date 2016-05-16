package cucumber.runtime.java.spring.springapplicationconfiguration;

import cucumber.api.java.en.Then;
import cucumber.runtime.java.spring.beans.BellyBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.junit.Assert.assertNotNull;

@SpringApplicationConfiguration
@IntegrationTest
public class BellyBootStepdefs {

    @Autowired
    private Boot belly;

    @Autowired
    private BellyBean bellyBean;

    @Then("^I have boot belly$")
    public void I_have_belly() throws Throwable {
        assertNotNull(belly);
    }

    @Then("^I have boot belly bean$")
    public void I_have_belly_bean() throws Throwable {
        assertNotNull(bellyBean);
    }

    @Configuration
    public static class Bellies {

        @Bean
        public Boot belly() {
            return new Boot();
        }

        @Bean
        public BellyBean bellyBean() {
            return new BellyBean();
        }
    }

    private static class Boot {


    }
}
