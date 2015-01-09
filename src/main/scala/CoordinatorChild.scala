import akka.actor.{ReceiveTimeout, Actor, ActorRef}
import scala.concurrent.duration._

class CoordinatorChild extends Actor {
  var requester: ActorRef = _

  context.setReceiveTimeout(Duration.Undefined)

  def waiting(nServers: Int, serverChildren: Set[ActorRef] = Set()): Receive = {
    case Yes() => {
      println("dostalem yes")
      val newServerChildren = serverChildren + sender()
      if (newServerChildren.size == nServers) {
        println("poslalem precommit")
        newServerChildren.foreach(c => c ! PreCommit())
        context.become(prepared(newServerChildren))
      } else {
        context.become(waiting(nServers, newServerChildren))
      }
    }
    case No() => {
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

  def prepared(serverChildren: Set[ActorRef], counter: Int = 0): Receive = {
    case Ack() => {
      if (counter + 1 == serverChildren.size) {
        requester ! Commit()
        serverChildren.foreach(c => c ! DoCommit())
        context.stop(self)
      } else {
        context.become(prepared(serverChildren, counter + 1))
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
      val servers = serverIds.map(id => context.actorSelection(id))
      servers.foreach(s => s ! CanCommit(objects.toSet))
      context.setReceiveTimeout(500 milliseconds)
      context.become(waiting(servers.size))
    }
  }
}
