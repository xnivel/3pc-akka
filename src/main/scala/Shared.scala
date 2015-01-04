class Shared[T](initial: T) {
  var value: T = initial
  def set(newValue: T): Unit = {
    value = newValue
  }
  def get: T = {
    value
  }
}
