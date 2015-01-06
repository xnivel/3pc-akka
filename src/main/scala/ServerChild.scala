/**
 * Created by xnivel on 06.01.15.
 */
import akka.actor.Actor
import akka.actor.ReceiveTimeout
import scala.concurrent.duration._

class ServerChild extends Actor {
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
    case CanCommit => {
      sender ! (Yes)
      context.setReceiveTimeout(100 milliseconds)
      context.become(waiting);
    }
    case ReceiveTimeout => {
      println("received a aaaaaaaaaaaaaaa")
    }

    case _ => println("received a messagae")
  }
}
