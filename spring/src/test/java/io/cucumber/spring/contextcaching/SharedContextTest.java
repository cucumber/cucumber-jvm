package io.cucumber.spring.contextcaching;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = ContextConfig.class)
class SharedContextTest {

    @Autowired
    ContextCounter contextCounter;

    @Test
    void contextCountIsOne() {
        // the context is shared between JUnit and Cucumber
        assertThat(contextCounter.getContextCount(), is(1));
    }

}
