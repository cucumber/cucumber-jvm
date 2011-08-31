package cucumber.runtime.java;

import cucumber.table.Table;
import cucumber.table.java.JavaBeanTableTransformer;

public class JavaBeanTableProcessor implements TableProcessor {

    private final Class<?> beanClass;

    public JavaBeanTableProcessor(Class<?> javaBeanClass) {
        this.beanClass = javaBeanClass;
    }

    @Override
    public Object process(Table table) {
        return new JavaBeanTableTransformer(this.beanClass).transformTable(table);
    }
}
