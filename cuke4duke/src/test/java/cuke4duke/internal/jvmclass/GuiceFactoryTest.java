package cuke4duke.internal.jvmclass;


import org.junit.Before;
import org.junit.Test;

import com.google.inject.AbstractModule;


import cuke4duke.internal.jvmclass.GuiceFactoryTest.SomeClass.SomeInnerClass;

public class GuiceFactoryTest {
	public static class SomeClass {
		public class SomeInnerClass {}
		public Object someInstanceOfAnonymousClass = new Object() {};
	}
	public class SomeModule extends AbstractModule {

		@Override
		protected void configure() {
			
		}
		
	}	

	GuiceFactory guiceFactory;
	@Before
	public void setUp() throws Throwable {
		System.setProperty("cuke4duke.guiceModule","cuke4duke.internal.jvmclass.SomeModule");
		guiceFactory = new GuiceFactory();
	}
	/**
	 * Tests that GuiceFactory does not fail if a class cannot be instantiated.
	 * One problem that occurs frequently is that inner classes get compiled
	 * to their own .class files and are picked up by GuiceFactory.
	 * As Guice cannot instantiate non-static inner classes and anonymous classes
	 * it throws an exception and aborts the instantiation process.
	 * By catching those exceptions Guice can continue and creates
	 * everything correctly. 
	 */
	@Test
	public void shouldNotAbortIfClassCannotBeInstantiated() {
		guiceFactory.addClass(SomeClass.class);
		
		SomeClass instance = new SomeClass();
		Class<? extends SomeInnerClass> someInnerClass = instance.new SomeInnerClass().getClass();
		guiceFactory.addClass(someInnerClass);
		guiceFactory.createObjects();
		
	}

}
