import akka.actor.{ActorRef, Actor, ReceiveTimeout}
import scala.concurrent.duration._

class ServerChild(sentObjects: Set[(VarRef, Shared[Int])], server: ActorRef) extends Actor {
  context.setReceiveTimeout(50 milliseconds)

  def aborting() = {
    println("send a AbortWithList")
    server ! new AbortWithList(sentObjects)
    context.stop(self)
  }

  def committing() = {
    server ! new WriteCommit(sentObjects)
    context.stop(self)
  }

  def waiting: Receive = {
    case (msg: PreCommit) => {
      println("ACK SEND")
      sender ! Ack()
      context.setReceiveTimeout(50 milliseconds)
      context.become(prepared);
    }
    case Abort() => {
      println("A1")
      aborting()
    }
    case ReceiveTimeout => {
      println("timeout1")
      aborting()
    }
    case (msg: Any) => {
      println("dostalemcos1 "+msg)
    }
  }

  def prepared: Receive = {
    case DoCommit() => {
      println("recDoCommit")
      committing()
    }
    case Abort => {
      println("A2")
      aborting()
    }
    case ReceiveTimeout => {
      println("timeout2")
      aborting()
    }
    case (msg: Any) => {
      println("dostalemcos2 "+msg)
    }
  }

  def receive = {
    case (msg:CanCommit) => {
      sender ! Yes()
      context.become(waiting);
    }
    case ReceiveTimeout => {
      println("timeout0")
      aborting()
    }
  }
}
