import akka.actor.Actor
import akka.actor.Actor.Receive

class Coordinator extends Actor {
  override def receive: Receive = {
    case _ => println("received a message")
  }
}
