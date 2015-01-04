import akka.actor.Actor

class Server(var objects: Map[String, Integer]) extends Actor {
  def receive = {
    case (name: String) => {
      println("received a message "+name)
    }
    case (msg: Read) => {
      sender ! (objects get msg.id get)
    }
    case (msg: Write) => {
      objects = objects.updated(msg.id, msg.newVal);
    }

    case _ => println("received a message")
  }
}
