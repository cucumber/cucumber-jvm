package cucumber.runtime.java;

import cuke4duke.annotation.After;
import cuke4duke.annotation.Before;
import cuke4duke.internal.Utils;
import cuke4duke.internal.java.annotation.CucumberAnnotation;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class ClasspathMethodScanner implements MethodScanner {
    public void scan(JavaMethodBackend javaMethodBackend, String packagePrefix) {
        try {
            Set<Class<Annotation>> cucumberAnnotations = findCucumberAnnotationClasses();
            for (Class<?> clazz : getClasses(packagePrefix)) {
                Method[] methods = clazz.getMethods();
                for (Method method : methods) {
                    if (isPublic(method.getModifiers())) {
                        scan(method, cucumberAnnotations, javaMethodBackend);
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Set<Class<Annotation>> findCucumberAnnotationClasses() throws IOException {
        Set<Class<Annotation>> result = new HashSet<Class<Annotation>>();
        Set<Class<?>> classes = getClasses("cuke4duke.annotation");
        for (Class<?> aClass : classes) {
            // Todo: generify and check type before casting
            Class<Annotation> annotationClass = (Class<Annotation>) aClass;
            result.add(annotationClass);
        }
        return result;
    }

    private void scan(Method method, Set<Class<Annotation>> cucumberAnnotationClasses, JavaMethodBackend javaMethodBackend) {
        for (Class<Annotation> cucumberAnnotationClass : cucumberAnnotationClasses) {
            Annotation annotation = method.getAnnotation(cucumberAnnotationClass);
            if(annotation != null) {
                if (isHook(annotation)) {
                    // TODO Add hook
                }

                if (annotation.annotationType().isAnnotationPresent(CucumberAnnotation.class)) {
                    Locale locale = Utils.localeFor(annotation.annotationType().getAnnotation(CucumberAnnotation.class).value());
                    try {
                        Method regexpMethod = annotation.getClass().getMethod("value");
                        String regexpString = (String) regexpMethod.invoke(annotation);
                        if (regexpString != null) {
                            Pattern pattern = Pattern.compile(regexpString);
                            javaMethodBackend.addStepDefinition(pattern, method, locale);
                        }
                    } catch (NoSuchMethodException ignore) {
                    } catch (IllegalAccessException ignore) {
                    } catch (InvocationTargetException ignore) {
                    }
                }
            }
        }
    }

    private boolean isHook(Annotation cucumberAnnotation) {
        return cucumberAnnotation.equals(Before.class) || cucumberAnnotation.equals(After.class);
    }

    Set<Class<?>> getClasses(String packagePrefix) throws IOException {
        Set<Class<?>> classes = new HashSet<Class<?>>();
        for (URL rootDir : classpathUrls(packagePrefix.replace(".", "/"))) {
            if (rootDir.getProtocol().equals("jar")) {
                addClassesFromJar(packagePrefix, classes, rootDir);
            } else {
                addClassesFromFilesystem(packagePrefix, classes, rootDir);
            }
        }
        return classes;
    }

    private void addClassesFromFilesystem(String packagePrefix, Set<Class<?>> classes, URL rootDir) {
        File dir = new File(rootDir.getFile());
        addClassesFromFilesystem(packagePrefix + ".", classes, dir);
    }

    private void addClassesFromFilesystem(String currentPackagePrefixWithDot, Set<Class<?>> classes, File dir) {
        for (File file : dir.listFiles()) {
            if (file.isDirectory()) {
                addClassesFromFilesystem(currentPackagePrefixWithDot + file.getName() + ".", classes, file);
            } else if (file.getName().endsWith(".class")) {
                String className = currentPackagePrefixWithDot + className(file.getName());
                try {
                    addClassIfPublic(classes, className);
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException("Failed to load class " + className + " from " + file.getAbsolutePath(), e);
                }
            }
        }
    }

    private void addClassesFromJar(String packagePrefix, Set<Class<?>> classes, URL jarDir) throws IOException {
        String url = jarDir.toExternalForm();
        String pathWithProtocol = url.substring(0, jarDir.toExternalForm().indexOf("!/"));
        String[] segments = pathWithProtocol.split(":");
        ZipFile jarFile = new ZipFile(segments[2]);
        Enumeration<? extends ZipEntry> entries = jarFile.entries();
        while (entries.hasMoreElements()) {
            ZipEntry jarEntry = entries.nextElement();
            String entryName = jarEntry.getName();
            if (entryName.endsWith(".class")) {
                String className = className(entryName);
                if (className.startsWith(packagePrefix)) {
                    try {
                        addClassIfPublic(classes, className);
                    } catch (ClassNotFoundException e) {
                        throw new RuntimeException("Failed to load class " + className + " from " + jarDir, e);
                    }
                }
            }
        }
    }

    private void addClassIfPublic(Set<Class<?>> classes, String className) throws ClassNotFoundException {
        Class<?> clazz = cl().loadClass(className);
        if (isPublic(clazz.getModifiers())) {
            classes.add(clazz);
        }
    }

    private static boolean isPublic(int modifiers) {
        return (modifiers & Modifier.PUBLIC) != 0;
    }

    private String className(String pathToClass) {
        return pathToClass.substring(0, pathToClass.length() - 6).replace("/", ".");
    }

    private List<URL> classpathUrls(String packagePrefix) throws IOException {
        return Collections.list(cl().getResources(packagePrefix));
    }

    private static ClassLoader cl() {
        return Thread.currentThread().getContextClassLoader();
    }

    public static List<URL> getClasspath() {
        return Arrays.asList(((URLClassLoader) cl()).getURLs());
    }
}
