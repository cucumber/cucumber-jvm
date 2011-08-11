package cucumber.runtime.java;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;
import java.util.Locale;

import junit.framework.Assert;

import org.junit.Test;

import cucumber.annotation.Pending;
import cucumber.annotation.Transform;
import cucumber.runtime.CucumberException;

public class JavaMethodTransformTest {
	
	@Test
	public void shouldTransformToUser() throws Exception {
		HasATransformMethod hasATransformMethod = new HasATransformMethod();
		Method transformMethod = hasATransformMethod.getClass().getMethod("transformToUser", String.class);
		ObjectFactory objectFactory = mock(ObjectFactory.class);
		when(objectFactory.getInstance(HasATransformMethod.class)).thenReturn(hasATransformMethod);
		JavaBackend backend = new JavaBackend(objectFactory, null);
		JavaMethodTransform javaMethodTransform = new JavaMethodTransform(transformMethod, backend);
		Object transformed = javaMethodTransform.transform("Cucumber", Locale.getDefault());
		Assert.assertEquals(User.class, transformed.getClass());
		Assert.assertEquals("Cucumber", ((User)transformed).getName());
	}
	
	@Test(expected=CucumberException.class)
	public void shouldThrowExceptionWhenUsingPendingTransform() throws Exception {
		HasAPendingTransformMethod hasATransformMethod = new HasAPendingTransformMethod();
		Method transformMethod = hasATransformMethod.getClass().getMethod("transformToUser", String.class);
		ObjectFactory objectFactory = mock(ObjectFactory.class);
		when(objectFactory.getInstance(HasAPendingTransformMethod.class)).thenReturn(hasATransformMethod);
		JavaBackend backend = new JavaBackend(objectFactory, null);
		new JavaMethodTransform(transformMethod, backend).transform("WhatEver", Locale.getDefault());
	}
	
	public class User {
		private String name;

		public String getName() {
			return name;
		}

		public User setName(String name) {
			this.name = name;
			return this;
		}
	}
	
	public class HasATransformMethod {
		@Transform
		public User transformToUser(String name) {
			return new User().setName(name);
		}
	}
	
	public class HasAPendingTransformMethod {
		@Transform
		@Pending("Testing")
		public User transformToUser(String name) {
			return new User().setName(name);
		}
	}
}
