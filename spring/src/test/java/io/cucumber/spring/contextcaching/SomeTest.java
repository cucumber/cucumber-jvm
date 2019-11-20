package cucumber.runtime.java.spring.contextcaching;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {ContextConfig.class})
public class SomeTest {
    @Autowired
    ContextCounter contextCounter;

    @Test
    public void contextCountIsOne() {
        assertThat(contextCounter.getContextCount(), is(1));
    }

}
