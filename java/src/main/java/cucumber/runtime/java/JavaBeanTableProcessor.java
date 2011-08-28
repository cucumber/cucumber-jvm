package cucumber.runtime.java;

import cucumber.runtime.TableArgumentProcessor;
import cucumber.table.Table;
import cucumber.table.java.JavaBeanTableTransformer;

public class JavaBeanTableProcessor implements TableArgumentProcessor {
    
    private final Class<?> beanClass;
    
    public JavaBeanTableProcessor(Class<?> javaBeanClass) {
        this.beanClass = javaBeanClass;
    }
    @Override
    public Object process(Table table) {
        return new JavaBeanTableTransformer(this.beanClass).transformTable(table);
    }
    public Class<?> getBeanClass() {
        return this.beanClass;
    }

}
