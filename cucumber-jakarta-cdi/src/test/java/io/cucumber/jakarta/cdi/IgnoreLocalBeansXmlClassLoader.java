package io.cucumber.jakarta.cdi;

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;

public class IgnoreLocalBeansXmlClassLoader extends ClassLoader {

    private static final String BEANS_XML_FILE = "META-INF/beans.xml";

    public IgnoreLocalBeansXmlClassLoader(ClassLoader parent) {
        super(parent);
    }

    @Override
    public Enumeration<URL> getResources(String name) throws IOException {
        Enumeration<URL> enumeration = super.getResources(name);
        if (BEANS_XML_FILE.equals(name) && enumeration.hasMoreElements()) {
            enumeration.nextElement();
        }
        return enumeration;
    }

    public static void setClassLoader(boolean ignoreLocalBeansXml) {
        ClassLoader threadClassLoader = Thread.currentThread().getContextClassLoader();
        if (ignoreLocalBeansXml && !(threadClassLoader instanceof IgnoreLocalBeansXmlClassLoader)) {
            Thread.currentThread().setContextClassLoader(new IgnoreLocalBeansXmlClassLoader(threadClassLoader));
        }
    }

    public static void restoreClassLoader() {
        ClassLoader threadClassLoader = Thread.currentThread().getContextClassLoader();
        if (threadClassLoader instanceof IgnoreLocalBeansXmlClassLoader) {
            Thread.currentThread().setContextClassLoader(threadClassLoader.getParent());
        }
    }

}
