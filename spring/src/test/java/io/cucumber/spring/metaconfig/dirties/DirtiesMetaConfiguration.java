package io.cucumber.spring.metaconfig.dirties;

import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)

@ContextConfiguration("classpath:cucumber.xml")
@DirtiesContext
public @interface DirtiesMetaConfiguration {

}
