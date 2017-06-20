package cucumber.runtime.java.weld.jmockit;

import java.lang.annotation.Annotation;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.ProcessAnnotatedType;

import org.jboss.weld.util.annotated.AnnotatedTypeWrapper;

final class AnnotatedTypeProcessor {

	public <T> void process(final ProcessAnnotatedType<T> processAnnotatedType) {

		final AnnotatedType<T> annotatedType = processAnnotatedType.getAnnotatedType();

		if (!Modifier.isFinal(annotatedType.getJavaClass().getModifiers())
				&& !annotatedType.getJavaClass().equals(JMockitInterceptor.class)) {
			
			final Set<Annotation> annotations = new HashSet<Annotation>(annotatedType.getAnnotations());
			annotations.add(new Annotation() {
				@Override
				public Class<? extends Annotation> annotationType() {
					return WithJMockit.class;
				}
			});

			final AnnotatedTypeWrapper<T> wrapper = new AnnotatedTypeWrapper<T>(
					annotatedType, annotations.toArray(new Annotation[annotations.size()]));

			processAnnotatedType.setAnnotatedType(wrapper);
		}
	}
	
}
