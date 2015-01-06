import akka.actor.{ActorSelection, ReceiveTimeout, Actor, ActorRef}
import scala.concurrent.duration._

class CoordinatorChild extends Actor {
  var requester: ActorRef = _
  var servers: Set[ActorSelection] = Set()
  var serverChildren: Set[ActorRef] = Set()
  var ackCounter = 0

  context.setReceiveTimeout(Duration.Undefined)

  def waiting: Receive = {
    case Yes => {
      println("dostalem yes")
      serverChildren = serverChildren + sender()
      if (serverChildren.size == servers.size)
        context.become(prepared)
    }
    case No => {
      requester ! Abort()
      serverChildren.foreach(c => c ! Abort())
      context.stop(self)
    }
    case ReceiveTimeout => {
      requester ! Abort()
      serverChildren.foreach(c => c ! Abort())
      context.stop(self)
    }
  }

  def prepared: Receive = {
    case Ack => {
      ackCounter += 1
      if (ackCounter == serverChildren.size) {
        requester ! Commit()
        serverChildren.foreach(c => c ! DoCommit())
        context.stop(self)
      }
    }
    case ReceiveTimeout => {
      requester ! Abort()
      serverChildren.foreach(c => c ! Abort())
      context.stop(self)
    }
  }

  def receive = {
    case CommitRequest(objects) => {
      requester = sender()
      val serverIds: Set[String] = objects.keys.map(o => o.serverId).toSet
      servers = serverIds.map(id => context.actorSelection(id))
      servers.foreach(s => s ! CanCommit(objects.toSet))
      context.setReceiveTimeout(500 milliseconds)
      context.become(waiting)
    }
  }
}
