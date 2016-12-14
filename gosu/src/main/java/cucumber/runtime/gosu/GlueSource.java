package cucumber.runtime.gosu;

import cucumber.runtime.io.Resource;
import gw.lang.reflect.ReflectUtil;
import gw.lang.reflect.gs.GosuClassPathThing;
import gw.lang.reflect.gs.IProgramInstance;

class GlueSource {

    /**
     * Performs minimal initialization of the Gosu TypeSystem; this is required for later calls to ReflectUtil#getClass
     */
    public GlueSource() {
        GosuClassPathThing.init();
    }

    /**
     * Given a glue Resource, calculates the FQN of the Gosu Program as a class, 
     * loads it via reflection and calls its evaluate method. 
     * @param glueScript
     */
    public void addGlueScript(Resource glueScript) {
        String className = glueScript.getClassName(".gsp");
        Class clazz = ReflectUtil.getClass(className).getBackingClass();
        try {
            ((IProgramInstance)(clazz.newInstance())).evaluate(null);
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

}
