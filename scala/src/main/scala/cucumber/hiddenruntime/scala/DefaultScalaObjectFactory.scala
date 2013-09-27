package cucumber.hiddenruntime.scala

import _root_.scala.collection.Map
import _root_.cucumber.runtime.CucumberException;
import cucumber.runtime.scala.ObjectFactory

class DefaultScalaObjectFactory extends ObjectFactory {
  var instances = Map[Class[_], Object]()
  
  override def start() = { /* nix*/ }
  override def stop() = { /* nix*/ }
  override def addClass(clazz: Class[_]) = { /* vorerst nicht*/ }
  
  override def getInstance[T](typ: Class[T]): T = {
    instances.get(typ).getOrElse(cacheNewInstance(typ)).asInstanceOf[T]
  }
  
  private def cacheNewInstance[T](typ:Class[T]):T={
    try{
      val instance = typ.newInstance//.getConstructor().newInstance(Array[Object]()).asInstanceOf[T]
      instances = instances + (typ -> instance.asInstanceOf[Object]) 
      instance
    } catch{
      case x:NoSuchMethodException=> throw new CucumberException(String.format("%s doesn't have an empty constructor.", typ), x);
      case x:Exception=>  throw new CucumberException(String.format("Failed to instantiate %s", typ), x);
    }
  }

}