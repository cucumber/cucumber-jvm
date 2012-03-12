package cucumber.runtime.java.spring.hooks;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import cucumber.annotation.Pending;
import cucumber.runtime.PendingException;

public class PendingAnnotationInterceptor implements MethodInterceptor {
	
	/**
	 * This is a {@link MethodInterceptor}, a kind of advice in AOP (aspect orient programming) that is wrapped around a method.
	 * 
     * Meaning, that after an AOP mechanism has manipulated byte code, or in the case of Spring AOP, constructed a JDK 1.4 {@link java.lang.reflect.Proxy} 
     * or a {@link net.sf.cglib.proxy.Proxy} Proxy, the target method is not directly invoked by callers, but that the logic is delegated to the {@link MethodInterceptor#invoke(MethodInvocation)} method,
     * which will allow the target method to be called, or not, depending on the {@link MethodInterceptor#invoke(MethodInvocation)} method's logic.
     *   
     * This intercepter will always throw an PendingException, and should only be applied to methods on objects with a {@link Pending} annotation.
     *
     * Typical spring usage would look like:<br/>
     * <pre>
     * 
     *&lt;aop:config&gt;    
     *   &lt;aop:advisor pointcut="@annotation(cucumber.annotation.Pending)" advice-ref="cucumber_throwsPendingException"/&gt;
     *&lt;/aop:config&gt;
     *  
     *&lt;bean id="cucumber_throwsPendingException" class="cucumber.runtime.java.spring.hooks.PendingAnnotationInterceptor"/&gt;
     *    
     * </pre>   
     * 
	 */
	public Object invoke(MethodInvocation invocation) throws Throwable {
		Pending pending = invocation.getThis().getClass().getMethod(invocation.getMethod().getName(), invocation.getMethod().getParameterTypes()).getAnnotation(Pending.class);
        throw new PendingException("On class: "+invocation.getThis().getClass() + " method: "+invocation.getMethod().getName()+" with Message "+ pending.value()) ;
	}
	
}
