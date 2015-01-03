class Transaction(client: Client) {
  def commit = ???

  def abort = ???

  /**
   * Read value of object from server
   */
  def read[T](p: Proxy[T]): T = ???

  /**
   * Write value of object to local buffer (will be sent on commit)
   */
  def write[T](p: Proxy[T], value: T): Unit = ???
}
