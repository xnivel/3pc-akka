import akka.actor.Actor
import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent.Await
import scala.concurrent.duration._

class Transaction extends Actor {
  implicit val timeout = Timeout(5 seconds)
  var buffer: Map[Proxy, Integer] = Map()

  def receive = {
    case _ => println("received a message")
  }

  def commit = {
    val coordinator = context.actorSelection("coordinator")
    val future = coordinator ? CommitRequest(buffer)
    Await.result(future, timeout.duration) match {
      case Commit() => println("Commited")
      case Abort() => throw new Exception("Got abort!")
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
    Await.result(future, timeout.duration).asInstanceOf[Integer]
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