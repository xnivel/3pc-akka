import akka.actor.{ReceiveTimeout, Actor, ActorRef}
import scala.concurrent.duration._

class CoordinatorChild(val clients: Set[ActorRef]) extends Actor {
  var requester: ActorRef = _
  var clientChildren: Set[ActorRef] = Set()
  var ackCounter = 0

  context.setReceiveTimeout(Duration.Undefined)

  def waiting: Receive = {
    case Yes => {
      clientChildren = clientChildren + sender()
      if (clientChildren.size == clients.size)
        context.become(prepared)
    }
    case No => {
      requester ! Abort()
      clientChildren.foreach(c => c ! Abort())
      context.stop(self)
    }
    case ReceiveTimeout => {
      requester ! Abort()
      clientChildren.foreach(c => c ! Abort())
      context.stop(self)
    }
  }

  def prepared: Receive = {
    case Ack => {
      ackCounter += 1
      if (ackCounter == clientChildren.size) {
        requester ! Commit()
        clientChildren.foreach(c => c ! Commit())
        context.stop(self)
      }
    }
    case ReceiveTimeout => {
      requester ! Abort()
      clientChildren.foreach(c => c ! Abort())
      context.stop(self)
    }
  }

  def receive = {
    case CommitRequest(objects) => {
      val requester = sender()
      clients.foreach(c => c ! CanCommit(objects))
      context.setReceiveTimeout(100 milliseconds)
      context.become(waiting)
    }
  }
}
