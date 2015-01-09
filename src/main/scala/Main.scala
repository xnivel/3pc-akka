import akka.actor.Actor
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

  println("Hello world")

  //pierwsze bajery

  // Create actor system.
  val system = ActorSystem("ItsTheFinalCountdown")

  // Create new actor reference (proxy).
  val map=Map("c"->((new Shared[Integer](2,0)),false));
  val server1 = system.actorOf(Props(new Server(map)), "Server1")

  val map2=Map("d"->((new Shared[Integer](1,0)),false));
  val server2 = system.actorOf(Props(new Server(map2)), "Server2")

  val future = server1 ? new Read("c")
  val result = Await.result(future, timeout.duration).asInstanceOf[Shared[Integer]]
  println(""+result.value)

//  val future2 = server1 ? new CanCommit(Set((new Proxy("c","c"),new Shared[Integer](3,1))))
//  val result2 = Await.result(future2, timeout.duration).asInstanceOf[Yes]
//
//
//  server1 ! new Write("c",12)
//
//  val future2 = server1 ? new Read("c")
//  val result2 = Await.result(future2, timeout.duration).asInstanceOf[Int]
//  println(""+result2)

  val coordinator = system.actorOf(Props[Coordinator])
  val v = Proxy(server1.path.toString, "c")
  val u = Proxy(server2.path.toString, "d")
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
