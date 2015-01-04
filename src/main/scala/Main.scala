import akka.actor.Actor
import akka.actor.ActorSystem
import akka.actor.Props
import scala.concurrent.Await
import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent.duration._


object Main extends App {

  implicit val timeout = Timeout(5 seconds)

  println("Hello world")

  //pierwsze bajery

  // Create actor system.
  val system = ActorSystem("ItsTheFinalCountdown")

  // Create new actor reference (proxy).
  val server1 = system.actorOf(Props(new Server(Map("c"->2))), "Server1")
  val client1 = system.actorOf(Props(new Client()), "Client1")

  val future = server1 ? ("read","c")
  val result = Await.result(future, timeout.duration).asInstanceOf[Int]
  println(""+result)

  server1 ! ("write","c",12)

  val future2 = server1 ? ("read","c")
  val result2 = Await.result(future2, timeout.duration).asInstanceOf[Int]
  println(""+result2)


}
