package cucumber.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import cucumber.runtime.java.JavaBeanTableProcessor;
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
@TableProcessorInfo(processorClass=JavaBeanTableProcessor.class)
public @interface JavaBeanClass {
    Class<?> value();
}
