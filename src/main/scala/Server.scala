import akka.actor.Actor

class Server(var objects: Map[String, Integer]) extends Actor {
  def waiting: Receive = {
    case PreCommit => {
      context.become(prepared);
    }
    case _ => {

    }
  }
  def prepared: Receive = {
    case DoCommit => {
      context.unbecome();
    }
    case _ => {

    }
  }
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
    case CanCommit => {
      context.become(waiting);
    }

    case _ => println("received a message")
  }
}
