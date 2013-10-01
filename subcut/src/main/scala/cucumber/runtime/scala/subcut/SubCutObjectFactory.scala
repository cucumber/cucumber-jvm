package cucumber.runtime.scala.subcut

import scala.reflect.runtime.{ universe => ru }
import scala.reflect.runtime.{ universe => ru }
import cucumber.runtime.scala.ObjectFactory
import com.escalatesoft.subcut.inject.BindingModule
import com.escalatesoft.subcut.inject.NewBindingModule
import com.escalatesoft.subcut.inject.MutableBindingModule
import cucumber.runtime.io.ResourceLoader
import cucumber.runtime.io.MultiLoader
import cucumber.runtime.Reflections
import cucumber.runtime.io.ResourceLoaderClassFinder
import cucumber.runtime.ClassFinder
import scala.collection.JavaConversions._
import cucumber.runtime.Utils

class SubCutObjectFactory extends ObjectFactory {
  val mirror = ru.runtimeMirror(getClass.getClassLoader)
  
  override def start() = { println("SubCutObjectFactory START") }

  override def stop() = { println("SubCutObjectFactory STOP") }

  override def addClass(clazz: Class[_]) = { println("SubCutObjectFactory addClass " + clazz.getName()) }

  override def getInstance[T](typ: Class[T]): T = {
    println("SubCutObjectFactory getInstance " + typ.getName())
    val bmName = (classOf[BindingModule]).getClass().getName
    val hasBindingModuleConstructor = typ.getDeclaredConstructors().exists{
      c => 
        c.getParameterTypes().toList.map(_.getName()) match{
          case bmName::Nil => true
          case _ => false
        }
    }
    if(hasBindingModuleConstructor){
      instantiateWithSubCutModule(typ)
    }else{
      typ.newInstance()
    }
  }

  /**
   * http://docs.scala-lang.org/overviews/reflection/overview.html
   * http://www.veebsbraindump.com/2013/03/scala-2-10-runtime-reflection-from-a-class-name/
   */
  def instantiateWithSubCutModule[T](cls: Class[T]): T = {
    val clsSym = mirror.classSymbol(cls)
    val clsReflect = mirror.reflectClass(clsSym)

    val constructor = clsSym.selfType.declaration(ru.nme.CONSTRUCTOR).asMethod
    val constrReflect = clsReflect.reflectConstructor(constructor)

    val resourceLoader = new MultiLoader(Thread.currentThread().getContextClassLoader());
    val classFinder: ClassFinder = new ResourceLoaderClassFinder(resourceLoader, this.getClass().getClassLoader())
    val reflections: Reflections = new Reflections(classFinder)

    // TODO load all binding modules and merge them together!
    val bindingModules = classFinder.getDescendants(classOf[BindingModule], "")
    if(bindingModules.isEmpty()) throw new Exception("Missing SubCut Module definition.")
//    val modules = bindingModules.map(m=>mirror.staticModule(m.getName())).map(a=>mirror.reflectModule(a).instance)
    
    val module = mirror.staticModule(bindingModules.head.getName())
    val obj = mirror.reflectModule(module).instance
    constrReflect(obj).asInstanceOf[T]
  }
} 

