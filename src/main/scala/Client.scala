import akka.actor.{ReceiveTimeout, Actor}

import scala.concurrent.duration._

class Client extends Actor {
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

  var currentObjects = Set[Proxy]()

  def receive = {
    case RegisterTransaction(objects) => {
      currentObjects = objects
    }
    case CanCommit(objects) => {
      if ((currentObjects & objects).isEmpty) {
        context.setReceiveTimeout(100 milliseconds)
        context.become(waiting)
        sender ! Yes()
      } else {
        sender ! No()
      }
    }
  }
}
