import akka.actor.{ReceiveTimeout, Actor}

import scala.concurrent.duration._


class Coordinator extends Actor {
  context.setReceiveTimeout(Duration.Undefined)
  def unbecome() = {
    context.setReceiveTimeout(Duration.Undefined)
    context.unbecome();
  }

  def waiting: Receive = {

    case Yes => {
      context.become(prepared);
    }
    case No => {
      unbecome()
    }
    case ReceiveTimeout => {
      unbecome()
    }
    case _ => {

    }
  }
  def prepared: Receive = {
    case Ack => {
      unbecome()
    }
    case ReceiveTimeout => {
      unbecome()
    }
    case _ => {

    }
  }
  def receive = {
    case Commit => {
      context.setReceiveTimeout(100 milliseconds)
      context.become(waiting);
    }
    case _ => println("received a message")
  }
}
