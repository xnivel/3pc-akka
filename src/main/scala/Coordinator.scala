import akka.actor.{Props, Actor}

class Coordinator extends Actor {
  def receive = {
    case CommitRequest(objects) => {
      val child = context.actorOf(Props[CoordinatorChild])
      child forward CommitRequest(objects)
    }
  }
}
