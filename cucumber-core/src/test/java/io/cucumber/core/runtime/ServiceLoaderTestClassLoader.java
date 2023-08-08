package io.cucumber.core.runtime;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.util.Collections;
import java.util.Enumeration;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Testing classloader for ServiceLoader. This classloader overrides the
 * META-INF/services/interface-class-name file with a custom definition.
 */
public class ServiceLoaderTestClassLoader extends URLClassLoader {
    Class<?> metaInfInterface;
    Class<?>[] implementingClasses;

    /**
     * Constructs a classloader which has no META-INF/services/metaInfInterface.
     *
     * @param metaInfInterface ServiceLoader interface
     */
    public ServiceLoaderTestClassLoader(Class<?> metaInfInterface) {
        this(metaInfInterface, (Class<?>[]) null);
    }

    /**
     * Constructs a fake META-INF/services/metaInfInterface file which contains
     * the provided array of classes. When the implementingClasses array is
     * null, the META-INF file will not be constructed. The classes from
     * implementingClasses are not required to implement the metaInfInterface.
     *
     * @param metaInfInterface    ServiceLoader interface
     * @param implementingClasses potential subclasses of the ServiceLoader
     *                            metaInfInterface
     */
    public ServiceLoaderTestClassLoader(Class<?> metaInfInterface, Class<?>... implementingClasses) {
        super(new URL[0], metaInfInterface.getClassLoader());
        if (!metaInfInterface.isInterface()) {
            throw new IllegalArgumentException("the META-INF service " + metaInfInterface + " should be an interface");
        }
        this.metaInfInterface = metaInfInterface;
        this.implementingClasses = implementingClasses;
    }

    @Override
    public Enumeration<URL> getResources(String name) throws IOException {
        if (name.equals("META-INF/services/" + metaInfInterface.getName())) {
            if (implementingClasses == null) {
                return Collections.emptyEnumeration();
            }
            URL url = new URL("foo", "bar", 99, "/foobar", new URLStreamHandler() {
                @Override
                protected URLConnection openConnection(URL u) {
                    return new URLConnection(u) {
                        @Override
                        public void connect() {
                        }

                        @Override
                        public InputStream getInputStream() {
                            return new ByteArrayInputStream(Stream.of(implementingClasses)
                                    .map(Class::getName)
                                    .collect(Collectors.joining("\n"))
                                    .getBytes());
                        }
                    };
                }
            });

            return new Enumeration<URL>() {
                boolean hasNext = true;

                @Override
                public boolean hasMoreElements() {
                    return hasNext;
                }

                @Override
                public URL nextElement() {
                    hasNext = false;
                    return url;
                }
            };
        }
        return super.getResources(name);
    }

}
