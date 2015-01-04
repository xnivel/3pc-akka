import akka.actor.Actor

class Coordinator extends Actor {
  def receive = {
    case _ => println("received a message")
  }
}
