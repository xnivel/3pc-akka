import akka.actor.Actor
import akka.actor.ActorSystem
import akka.actor.Props
import scala.concurrent.Await
import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent.duration._
import Transaction.transaction

object Main extends App {

  implicit val timeout = Timeout(5 seconds)

  println("Hello world")

  //pierwsze bajery

  // Create actor system.
  val system = ActorSystem("ItsTheFinalCountdown")

  // Create new actor reference (proxy).
  val map=Map("c"->((new Shared[Integer](2,0)),false));
  val server1 = system.actorOf(Props(new Server(map)), "Server1")

  val future = server1 ? new Read("c")
  val result = Await.result(future, timeout.duration).asInstanceOf[Shared[Integer]]
  println(""+result.value)
//
//  server1 ! new Write("c",12)
//
//  val future2 = server1 ? new Read("c")
//  val result2 = Await.result(future2, timeout.duration).asInstanceOf[Int]
//  println(""+result2)

  val v = Proxy("Server1", "c")
  transaction(system) { tx =>
    val x = tx.read(v)
    println(x)
    tx.write(v, 5)
    println("wrote 5")
  }
}
