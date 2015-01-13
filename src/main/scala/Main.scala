import akka.actor.ActorSystem
import akka.actor.Props
import scala.concurrent.Await
import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent.duration._
import Transaction.transaction
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

object Main extends App {

  implicit val timeout = Timeout(5 seconds)

  val system = ActorSystem("ItsTheFinalCountdown")
  val server1 = system.actorOf(Props(new Server()), "Server1")
  val server2 = system.actorOf(Props(new Server()), "Server2")

  val future = server1 ? new Read("c")
  val result = Await.result(future, timeout.duration).asInstanceOf[Shared[Int]]
  println(""+result.value)

  val coordinator = system.actorSelection(system.actorOf(Props[Coordinator]).path)
  val v = VarRef(server1.path.toString, "c")
  val u = VarRef(server2.path.toString, "d")
  val txBlock = () => transaction(system, coordinator) { tx =>
    val x = tx.read(v)
    val y = tx.read(u)
    println(x)
    println(y)
    tx.write(v, 5)
    println("wrote 5")
    tx.write(u, 6)
    println("wrote 6")
  }
  Future { txBlock() }
  Future { txBlock() }
}
