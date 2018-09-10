/*
 * Copyright 2002-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.cucumber.spring;

import java.util.Set;

import org.springframework.beans.BeanUtils;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.test.context.BootstrapContext;
import org.springframework.test.context.BootstrapWith;
import org.springframework.test.context.CacheAwareContextLoaderDelegate;
import org.springframework.test.context.TestContextBootstrapper;
import org.springframework.test.context.cache.ContextCache;
import org.springframework.test.context.cache.DefaultCacheAwareContextLoaderDelegate;
import org.springframework.test.context.cache.DefaultContextCache;
import org.springframework.test.context.support.DefaultBootstrapContext;
import org.springframework.util.ClassUtils;

/**
 * {@code BootstrapUtils} is a collection of utility methods to assist with
 * bootstrapping the <em>Spring TestContext Framework</em>.
 *
 * @author Sam Brannen
 * @author Phillip Webb
 * @since 4.1
 * @see BootstrapWith
 * @see BootstrapContext
 * @see TestContextBootstrapper
 */
abstract class FixBootstrapUtils {

    private static ThreadLocal<ContextCache> contextCache = new ThreadLocal<ContextCache>(){
        @Override
        protected ContextCache initialValue() {
            return new DefaultContextCache();
        }
    };

    private static final String DEFAULT_TEST_CONTEXT_BOOTSTRAPPER_CLASS_NAME =
			"org.springframework.test.context.support.DefaultTestContextBootstrapper";

	private static final String DEFAULT_WEB_TEST_CONTEXT_BOOTSTRAPPER_CLASS_NAME =
			"org.springframework.test.context.web.WebTestContextBootstrapper";

	private static final String WEB_APP_CONFIGURATION_ANNOTATION_CLASS_NAME =
			"org.springframework.test.context.web.WebAppConfiguration";

    static BootstrapContext createBootstrapContext(Class<?> testClass) {
        CacheAwareContextLoaderDelegate contextLoader = new DefaultCacheAwareContextLoaderDelegate(contextCache.get());
        return new DefaultBootstrapContext(testClass, contextLoader);
    }

    /**
	 * Resolve the {@link TestContextBootstrapper} type for the test class in the
	 * supplied {@link BootstrapContext}, instantiate it, and provide it a reference
	 * to the {@link BootstrapContext}.
	 * <p>If the {@link BootstrapWith @BootstrapWith} annotation is present on
	 * the test class, either directly or as a meta-annotation, then its
	 * {@link BootstrapWith#value value} will be used as the bootstrapper type.
	 * Otherwise, either the
	 * {@link org.springframework.test.context.support.DefaultTestContextBootstrapper
	 * DefaultTestContextBootstrapper} or the
	 * {@link org.springframework.test.context.web.WebTestContextBootstrapper
	 * WebTestContextBootstrapper} will be used, depending on the presence of
	 * {@link org.springframework.test.context.web.WebAppConfiguration @WebAppConfiguration}.
	 * @param bootstrapContext the bootstrap context to use
	 * @return a fully configured {@code TestContextBootstrapper}
	 */
	static TestContextBootstrapper resolveTestContextBootstrapper(BootstrapContext bootstrapContext) {
		Class<?> testClass = bootstrapContext.getTestClass();

		Class<?> clazz = null;
		try {
			clazz = resolveExplicitTestContextBootstrapper(testClass);
			if (clazz == null) {
				clazz = resolveDefaultTestContextBootstrapper(testClass);
			}
			TestContextBootstrapper testContextBootstrapper =
					BeanUtils.instantiateClass(clazz, TestContextBootstrapper.class);
			testContextBootstrapper.setBootstrapContext(bootstrapContext);
			return testContextBootstrapper;
		}
		catch (IllegalStateException ex) {
			throw ex;
		}
		catch (Throwable ex) {
			throw new IllegalStateException("Could not load TestContextBootstrapper [" + clazz +
					"]. Specify @BootstrapWith's 'value' attribute or make the default bootstrapper class available.",
					ex);
		}
	}

	private static Class<?> resolveExplicitTestContextBootstrapper(Class<?> testClass) {
		Set<BootstrapWith> annotations = AnnotatedElementUtils.findAllMergedAnnotations(testClass, BootstrapWith.class);
		if (annotations.size() < 1) {
			return null;
		}
		if (annotations.size() > 1) {
			throw new IllegalStateException(String.format(
				"Configuration error: found multiple declarations of @BootstrapWith for test class [%s]: %s",
				testClass.getName(), annotations));
		}
		return annotations.iterator().next().value();
	}

	private static Class<?> resolveDefaultTestContextBootstrapper(Class<?> testClass) throws Exception {
		ClassLoader classLoader = FixBootstrapUtils.class.getClassLoader();
		AnnotationAttributes attributes = AnnotatedElementUtils.findMergedAnnotationAttributes(testClass,
			WEB_APP_CONFIGURATION_ANNOTATION_CLASS_NAME, false, false);
		if (attributes != null) {
			return ClassUtils.forName(DEFAULT_WEB_TEST_CONTEXT_BOOTSTRAPPER_CLASS_NAME, classLoader);
		}
		return ClassUtils.forName(DEFAULT_TEST_CONTEXT_BOOTSTRAPPER_CLASS_NAME, classLoader);
	}

}
