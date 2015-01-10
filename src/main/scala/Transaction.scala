import akka.actor.ActorRef
import akka.actor.ActorSystem
import akka.pattern.ask
import akka.util.Timeout
import scala.annotation.tailrec
import scala.concurrent.Await
import scala.concurrent.duration._

class Transaction(val system: ActorSystem, val coordinator: ActorRef) {
  implicit val timeout = Timeout(5 seconds)
  var buffer: Map[Proxy, Shared[Integer]] = Map()
  
  def commit: Boolean = {
    val future = coordinator ? CommitRequest(buffer)
    Await.result(future, timeout.duration) match {
      case Commit() => {
        println("Commited")
        true
      }
      case Abort() => {
        println("Aborted")
        false
      }
    }
  }

  /**
   * Read value of object from server
   */
  def read(p: Proxy): Integer = {
    val server = system.actorSelection(p.serverId)
    val future = server ? Read(p.variableId)
    val shared = Await.result(future, timeout.duration).
                       asInstanceOf[Shared[Integer]]
    buffer = buffer.updated(p, shared)
    shared.value
  }

  /**
   * Write value of object to local buffer (will be sent on commit)
   */
  def write(p: Proxy, value: Integer): Unit = {
    val Shared(_, version) = buffer(p)
    buffer = buffer.updated(p, Shared(value, version + 1))
  }
}

object Transaction {
  val random = new scala.util.Random
  /**
   * Usage:
   *
   * import Transaction.transaction
   *
   * val system = ActorSystem("MySystem")
   * val v = Proxy("serverId", "variableId")
   * val coordinator = system.actorSelection(...)
   * transaction(system, coordinator) { tx =>
   * val x = tx.read(v)
   * tx.write(v, x + 1)
   * }
   */
  @tailrec
  def transaction(system: ActorSystem, coordinator: ActorRef, loopnr: Int)
                 (codeBlock: Transaction => Unit): Unit = {
    val tx = new Transaction(system, coordinator)
    codeBlock(tx)
    val success = tx.commit
    if (!success)
    {
      Thread sleep ((random.nextInt(150)+150)*loopnr)
      transaction(system, coordinator,loopnr+1)(codeBlock)

    }
  }

  def transaction(system: ActorSystem, coordinator: ActorRef)
                 (codeBlock: Transaction => Unit): Unit = {
    transaction(system, coordinator,1)(codeBlock)

  }
}
