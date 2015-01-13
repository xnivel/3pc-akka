import akka.actor.ActorSystem
import akka.util.Timeout
import scala.concurrent.duration._
import Transaction.transaction
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

object ClientMain extends App {
  implicit val timeout = Timeout(5 seconds)

  val coordinatorHost = args(0)
  val coordinatorPort = args(1)
  val server1Host = args(2)
  val server1Port = args(3)
  val server2Host = args(4)
  val server2Port = args(5)

  val system = ActorSystem("Main")
  val coordinator = system.actorSelection(s"akka.tcp://CoordinatorSystem@$coordinatorHost:$coordinatorPort/user/coordinator")
  val server1Path = s"akka.tcp://ServerSystem@$server1Host:$server1Port/user/server1"
  val server2Path = s"akka.tcp://ServerSystem@$server2Host:$server2Port/user/server2"

  val v = VarRef(server1Path, "c")
  val u = VarRef(server2Path, "d")
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
