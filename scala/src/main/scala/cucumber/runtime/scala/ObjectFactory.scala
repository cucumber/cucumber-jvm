package cucumber.runtime.scala

trait ObjectFactory {
  def start()

  def stop();

  def addClass(clazz: Class[_]);

  def getInstance[T](typ: Class[T]): T;
}