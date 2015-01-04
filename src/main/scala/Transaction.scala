import akka.actor.Actor
import akka.pattern.ask
import scala.concurrent.Await
import scala.concurrent.duration._

class Transaction extends Actor {
  var buffer: Map[Proxy, Integer] = Map()

  def receive = {
    case _ => println("received a message")
  }

  def commit = {
    val coordinator = context.actorSelection("coordinator")
    coordinator ? CommitRequest(buffer) andThen {
      case Commit => println("Commited")
      case Abort => throw new Exception("Got abort!")
    }
  }

  def abort = {
    println("Aborted")
  }

  /**
   * Read value of object from server
   */
  def read(p: Proxy): Integer = {
    val server = context.actorSelection(p.serverId)
    val future = server ? Read(p.variableId)
    Await.result(future, 5 seconds).asInstanceOf[Integer]
  }

  /**
   * Write value of object to local buffer (will be sent on commit)
   */
  def write(p: Proxy, value: Integer): Unit = {
    buffer = buffer.updated(p, value)
  }
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