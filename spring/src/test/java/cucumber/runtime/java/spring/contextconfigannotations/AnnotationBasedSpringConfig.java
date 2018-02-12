package cucumber.runtime.java.spring.contextconfigannotations;

import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

@Configuration
@ComponentScan(basePackages={"cucumber.runtime.java.spring.contextconfigannotations"})
public class AnnotationBasedSpringConfig {

	@Bean
	@Scope("cucumber-glue")
	public ToInject toInject1(){
		return Mockito.mock(ToInject.class);
	}
	
	
	
}
