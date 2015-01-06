import akka.actor.ActorRef
import akka.actor.ActorSystem
import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent.Await
import scala.concurrent.duration._

class Transaction(val system: ActorSystem) {
  implicit val timeout = Timeout(5 seconds)
  var buffer: Map[Proxy, Integer] = Map()
  
  def commit = {
    val coordinator = system.actorSelection("coordinator")
    val future = coordinator ? CommitRequest(buffer.keys.toSet)
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
    val server = system.actorSelection(p.serverId)
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
   * val system = ActorSystem("MySystem")
   * val client = system.actorOf(Props(new Client()), "client1")
   * val v = Proxy("serverId", "variableId")
   * transaction(system, client, List(v)) { tx =>
   *   val x = tx.read(v)
   *   tx.write(v, x + 1)
   * }
   */
  def transaction(system: ActorSystem, client: ActorRef, objects: Set[Proxy])
                 (codeBlock: Transaction => Unit): Unit = {
    client ! RegisterTransaction(objects)
    val tx = new Transaction(system)
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