import akka.actor.Actor

class Server(val objects: Map[String, Integer]) extends Actor {
  def receive = {
    case _ => println("received a message")
  }
}
