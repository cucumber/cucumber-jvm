package cucumber.table.java;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang3.reflect.ConstructorUtils;

import cucumber.runtime.transformers.TransformationException;
import cucumber.runtime.transformers.Transformers;
import cucumber.table.Table;
import cucumber.table.TableTransformer;

public class JavaBeanTableTransformer implements TableTransformer {
    
    private Class<?> beanClass;
    private Map<String, PropertyDescriptor> beanPropertyDescriptors;
    private final Transformers transformers = new Transformers();
    
    public JavaBeanTableTransformer(Class<?> beanClass) {
        super();
        this.beanClass = beanClass;
    }

    @Override
    public <T> List<T> transformTable(Table table) {
        initializeTableMappers(table);
        List<T> list = new ArrayList<T>();
        for (Map<String, Object> map : table.hashes()) {
            list.add((T) createAndPopulateNewBean(map));
        }
        return list;
    }

    private void initializeTableMappers(Table table) {
        table.mapHeaders(new JavaBeanPropertyHeaderMapper());
        for (String propertyName : getBeanPropertyDescriptors().keySet()) {
            PropertyDescriptor propertyDescriptor = getBeanPropertyDescriptors().get(propertyName);
            table.mapColumn(propertyName, this.transformers.getTransformer(propertyDescriptor.getPropertyType()));
        }
    }
    
    private Object createAndPopulateNewBean(Map<String, Object> fieldValues) {
        Object newObject = createNewBean();
        for (Map.Entry<String, Object> entry : fieldValues.entrySet()) {
            setBeanProperty(newObject, entry);
        }
        return newObject;
    }
    
    private void setBeanProperty(Object beanInstance, Entry<String, Object> headerValueEntry) {
        PropertyDescriptor propertyDescriptor = getBeanPropertyDescriptors().get(headerValueEntry.getKey());
        Method setter = propertyDescriptor.getWriteMethod();
        try {
            setter.invoke(beanInstance, headerValueEntry.getValue());
        } catch (IllegalArgumentException e) {
            throw new TransformationException("Unable to transformable Table");
        } catch (IllegalAccessException e) {
            throw new TransformationException("Unable to transformable Table");
        } catch (InvocationTargetException e) {
            throw new TransformationException("Unable to transformable Table");
        }
    }

    private Map<String, PropertyDescriptor> getBeanPropertyDescriptors() {
        if (this.beanPropertyDescriptors == null) {
            this.beanPropertyDescriptors = new HashMap<String, PropertyDescriptor>();
            for (PropertyDescriptor propertyDescriptor : getBeanInfo().getPropertyDescriptors()) {
                this.beanPropertyDescriptors.put(propertyDescriptor.getName(), propertyDescriptor);
            }
        }
        return this.beanPropertyDescriptors;
    }

    private Object createNewBean() {
        try {
            Constructor<?> matchingAccessibleConstructor;
            matchingAccessibleConstructor = beanClass.getConstructor();
            return matchingAccessibleConstructor.newInstance();
        } catch (SecurityException e) {
            throw new TransformationException("Unable to transformable Table");
        } catch (NoSuchMethodException e) {
            throw new TransformationException("Unable to transformable Table");
        } catch (IllegalArgumentException e) {
            throw new TransformationException("Unable to transformable Table");
        } catch (InstantiationException e) {
            throw new TransformationException("Unable to transformable Table");
        } catch (IllegalAccessException e) {
            throw new TransformationException("Unable to transformable Table");
        } catch (InvocationTargetException e) {
            throw new TransformationException("Unable to transformable Table");
        }
    }

    private BeanInfo getBeanInfo() {
        try {
            return Introspector.getBeanInfo(this.beanClass, Object.class);
        } catch (IntrospectionException e) {
            throw new TransformationException("Unable to transformable Table");
        }
    }

}
