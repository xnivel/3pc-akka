import akka.actor.Actor
import akka.actor.ReceiveTimeout
import scala.concurrent.duration._

class Server(var objects: Map[String, Integer]) extends Actor {
  context.setReceiveTimeout(Duration.Undefined)
  def unbecome() = {
    context.setReceiveTimeout(Duration.Undefined)
    context.unbecome();
  }

  def waiting: Receive = {

    case PreCommit => {
      context.become(prepared);
    }
    case Abort => {
      unbecome()
    }
    case ReceiveTimeout => {
      unbecome()
    }
    case _ => {

    }
  }
  def prepared: Receive = {
    case DoCommit => {
      unbecome()
    }
    case Abort => {
      unbecome()
    }
    case ReceiveTimeout => {
      unbecome()
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
      context.setReceiveTimeout(100 milliseconds)
      context.become(waiting);
    }
    case ReceiveTimeout => {
      println("received a aaaaaaaaaaaaaaa")
    }

    case _ => println("received a messagae")
  }
}
