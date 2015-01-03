import akka.actor.Actor

class Client extends Actor {
  def receive = {
    case _ => println("received a message")
  }

  /**
   * Usage:
   *
   * val v = new Proxy("serverId", "variableId")
   * val client = new Client()
   * client.transaction { tx =>
   *   val x = tx.read(v)
   *   tx.write(v, x + 1)
   * }
   */
  def transaction(codeBlock: Transaction => Unit): Unit = {
    val tx = new Transaction(this)
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
