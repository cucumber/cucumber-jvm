package cucumber.runtime.java.spring;

import java.util.Collection;
import java.util.HashSet;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.GenericXmlApplicationContext;
import org.springframework.test.context.TestContextManager;

import cucumber.runtime.CucumberException;
import cucumber.runtime.java.ObjectFactory;

/**
 * Spring based implementation of ObjectFactory.
 * <p/>
 * <p>
 * <ul>
 * <li>It uses TestContextManager to create and prepare test instances. Configuration via: @ContextConfiguration
 * </li>
 * <li>It also uses a context which contains the step definitions and is reloaded for each
 * scenario.</li>
 * </ul>
 * </p>
 * <p/>
 * <p>
 * Application beans are accessible from the step definitions using autowiring
 * (with annotations).
 * </p>
 */
public class SpringFactory implements ObjectFactory {

	private static ConfigurableApplicationContext applicationContext;

	private final Collection<Class<?>> stepClasses = new HashSet<Class<?>>();

	public SpringFactory() {
	}

	static {
		applicationContext = new GenericXmlApplicationContext(
				"cucumber/runtime/java/spring/cucumber-glue.xml");
		applicationContext.registerShutdownHook();
	}

	@Override
	public void addClass(final Class<?> stepClass) {
		if (!stepClasses.contains(stepClass)) {
			stepClasses.add(stepClass);

			BeanDefinitionRegistry registry = (BeanDefinitionRegistry) applicationContext
					.getAutowireCapableBeanFactory();
			registry.registerBeanDefinition(stepClass.getName(),
					BeanDefinitionBuilder.genericBeanDefinition(stepClass)
							.setScope(GlueCodeScope.NAME).getBeanDefinition());

		}
	}

	@Override
	public void start() {
		GlueCodeContext.INSTANCE.start();
	}

	@Override
	public void stop() {
		GlueCodeContext.INSTANCE.stop();
		applicationContext.getBeanFactory().destroySingletons();
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getInstance(final Class<T> type) {
		if (!applicationContext.getBeanFactory().containsSingleton(type.getName())) {
			applicationContext.getBeanFactory().registerSingleton(type.getName(), getTestInstance(type));
		}
		return applicationContext.getBean(type);
	}

	private <T> T getTestInstance(final Class<T> type) {
		T instance = null;
		try {
			instance = createTest(type);
			TestContextManager contextManager = new TestContextManager(type);
			contextManager.prepareTestInstance(instance);
		} catch (Exception e) {
			new CucumberException(e.getMessage(), e);
		}
		return instance;
	}

	@SuppressWarnings("unchecked")
	protected <T> T createTest(Class<T> type) throws Exception {
		return (T) type.getConstructors()[0].newInstance();
	}

}
