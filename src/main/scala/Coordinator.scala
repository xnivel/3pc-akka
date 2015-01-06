import akka.actor.{Props, Actor, ActorRef}

class Coordinator(val clients: Set[ActorRef]) extends Actor {

  def receive = {
    case CommitRequest(objects) => {
      val child = context.actorOf(Props(classOf[CoordinatorChild], clients))
      child forward CommitRequest(objects)
    }
    case _ => println("received a message")
  }
}
