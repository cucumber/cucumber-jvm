package cucumber.examples.java.tycho.calculator;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

import cucumber.examples.java.tycho.calculator.api.CalculatorService;
import cucumber.examples.java.tycho.calculator.impl.CalculatorServiceImpl;

public class Activator implements BundleActivator {

    private volatile ServiceRegistration<CalculatorService> serviceRegistration;

    @Override
    public void start(BundleContext context) throws Exception {
        serviceRegistration = context.registerService(CalculatorService.class, new CalculatorServiceImpl(), null);
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        if (serviceRegistration != null) {
            serviceRegistration.unregister();
            serviceRegistration = null;
        }
    }
}
