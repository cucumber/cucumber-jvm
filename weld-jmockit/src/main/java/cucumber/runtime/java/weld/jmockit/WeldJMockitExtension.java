package cucumber.runtime.java.weld.jmockit;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AnnotatedMethod;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessAnnotatedType;
import javax.enterprise.inject.spi.WithAnnotations;

import cucumber.api.java.After;
import cucumber.api.java.Before;
import cucumber.runtime.ClassFinder;
import cucumber.runtime.io.MultiLoader;
import cucumber.runtime.io.ResourceLoader;
import cucumber.runtime.io.ResourceLoaderClassFinder;
import cucumber.runtime.java.StepDefAnnotation;

public final class WeldJMockitExtension implements Extension {

	private static final String CUCUMBER_API_PACKAGE = "cucumber.api";

	private final Collection<Class<? extends Annotation>> stepDefAnnotationClasses = 
			new HashSet<Class<? extends Annotation>>();
	
	private final AnnotatedTypeProcessor processor = new AnnotatedTypeProcessor();
		
	public WeldJMockitExtension() {
		this.findStepDefAnnotations();
	}
	
	public <T> void processAnnotatedTypeForHooks(
			@Observes
			@WithAnnotations( { After.class, Before.class } )
			final ProcessAnnotatedType<T> processAnnotatedType) {
		this.processor.process(processAnnotatedType);
	}
	
	public <T> void processAnnotatedTypeGeneric(@Observes final ProcessAnnotatedType<T> processAnnotatedType) {
		final Iterator<AnnotatedMethod<? super T>> methodsIterator = 
				processAnnotatedType.getAnnotatedType().getMethods().iterator();
		
		while(methodsIterator.hasNext()) {
			final AnnotatedMethod<? super T> method = methodsIterator.next();
			if (this.processIfMethodHasStepDefAnnotation(processAnnotatedType, method)) {
				break;
			}
		}
	}
	
	private <T> boolean processIfMethodHasStepDefAnnotation(
			final ProcessAnnotatedType<T> processAnnotatedType, final AnnotatedMethod<? super T> method) {
		
		if (this.isStepDefAnnotationPresent(processAnnotatedType, method)) {
			this.processor.process(processAnnotatedType);
			return true;
		}
		return false;
	}

	private <T> boolean isStepDefAnnotationPresent(
			final ProcessAnnotatedType<T> processAnnotatedType, final AnnotatedMethod<? super T> method) {
		
		final Iterator<Class<? extends Annotation>> annotationIterator = this.stepDefAnnotationClasses.iterator();
		while (annotationIterator.hasNext()) {
			final Class<? extends Annotation> annotationClass = annotationIterator.next();
			if (method.isAnnotationPresent(annotationClass)) {
				return true;
			}
		}
		return false;
	}

	private void findStepDefAnnotations() {
		final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		final ResourceLoader resourceLoader = new MultiLoader(classLoader);
        final ClassFinder classFinder = new ResourceLoaderClassFinder(resourceLoader, classLoader);
        final Collection<Class<? extends Annotation>> annotations = 
        		classFinder.getDescendants(Annotation.class, CUCUMBER_API_PACKAGE);
        for (Class<? extends Annotation> annotationClass : annotations) {
        	if (annotationClass.isAnnotationPresent(StepDefAnnotation.class)) {
        		this.stepDefAnnotationClasses.add(annotationClass);
        	}
        }
	}
}
