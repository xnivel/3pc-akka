import akka.actor.ActorSystem
import akka.util.Timeout
import scala.concurrent.duration._
import Transaction.transaction
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

object ClientMain extends App {
  implicit val timeout = Timeout(5 seconds)

  val system = ActorSystem("Main")
  val coordinator = system.actorSelection("akka.tcp://CoordinatorSystem@127.0.0.1:9000/user/coordinator")
  val server1Path = "akka.tcp://ServerSystem@127.0.0.1:9001/user/server1"
  val server2Path = "akka.tcp://ServerSystem@127.0.0.1:9002/user/server2"

  val v = Proxy(server1Path, "c")
  val u = Proxy(server2Path, "d")
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
