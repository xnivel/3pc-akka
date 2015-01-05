import akka.actor.Actor

class Client extends Actor {
  var currentObjects = Set[Proxy]()
  def receive = {
    case RegisterTransaction(objects) => {
      currentObjects = objects
      context.become(transactionInProgress)
    }
  }

  def transactionInProgress: Receive = {
    case _ => println("received a message")
  }
}
