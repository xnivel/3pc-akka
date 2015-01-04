import akka.actor.Actor
import akka.actor.ActorSystem
import akka.actor.Props
import akka.actor.Status.Success
import akka.actor.ActorRef

object Main extends App {
  println("Hello world")

  //pierwsze bajery

  // Create actor system.
  val system = ActorSystem("ItsTheFinalCountdown")

  // Create new actor reference (proxy).
  val server1 = system.actorOf(Props(new Server(Map("c"->1))), "Server1")
  val client1 = system.actorOf(Props(new Client()), "Client1")

  server1 ! 10


}
