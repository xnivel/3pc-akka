import akka.actor.Actor

class Server(val objects: Map[String, Shared[Integer]]) extends Actor {
  def receive = {
    case _ => println("received a message")
  }
}
