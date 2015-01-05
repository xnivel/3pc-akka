import akka.actor.Actor
import akka.actor.ReceiveTimeout
import scala.concurrent.duration._

class Server(var objects: Map[String, Integer]) extends Actor {
  context.setReceiveTimeout(100 milliseconds)

  def waiting: Receive = {

    case PreCommit => {
      context.become(prepared);
    }
    case Abort => {
      context.unbecome();
    }
    case ReceiveTimeout => {
      context.unbecome();
    }
    case _ => {

    }
  }
  def prepared: Receive = {
    case DoCommit => {
      context.unbecome();
    }
    case Abort => {
      context.unbecome();
    }
    case ReceiveTimeout => {
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
