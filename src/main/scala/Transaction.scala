import akka.actor.Actor

class Transaction extends Actor {
  def receive = {
    case _ => println("received a message")
  }

  def commit = ???

  def abort = ???

  /**
   * Read value of object from server
   */
  def read(p: Proxy): Integer = ???

  /**
   * Write value of object to local buffer (will be sent on commit)
   */
  def write(p: Proxy, value: Integer): Unit = ???
}

object Transaction {
  /**
   * Usage:
   *
   * import Transaction.transaction
   *
   * val v = new Proxy("serverId", "variableId")
   * transaction { tx =>
   *   val x = tx.read(v)
   *   tx.write(v, x + 1)
   * }
   */
  def transaction(codeBlock: Transaction => Unit): Unit = {
    val tx = new Transaction
    try {
      codeBlock(tx)
      tx.commit
    } catch {
      case ex: Throwable => {
        tx.abort
        throw ex
      }
    }
  }
}