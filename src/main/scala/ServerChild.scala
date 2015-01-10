/**
 * Created by xnivel on 06.01.15.
 */
import akka.actor.{ActorRef, Actor, ReceiveTimeout}
import scala.concurrent.duration._

class ServerChild(sentObjects:Set[(Proxy,Shared[Int])],server: ActorRef) extends Actor {
  //val objects: Set[(Proxy,Shared[Int])]=sendedObjects;
//  context.setReceiveTimeout(Duration.Undefined)
  context.setReceiveTimeout(50 milliseconds)

  def unbecome() = {
//    context.setReceiveTimeout(Duration.Undefined)
//    context.unbecome();
  }
  def Aborting() = {
    println("send a AbortWithList")
    //val proxyList = sentObjects.map(o => o._1)
    server ! new AbortWithList(sentObjects)
    context.stop(self)
  }
  def Commiting()={
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
      unbecome()
      Aborting()
    }
    case ReceiveTimeout => {
      println("timeout1")
      unbecome()
      Aborting()
    }
    case (msg: Any) => {
      println("dostalemcos1 "+msg)

    }
  }
  def prepared: Receive = {
    case DoCommit() => {
      println("recDoCommit")
      unbecome()
      Commiting()
    }
    case Abort => {
      println("A2")
      unbecome()
      Aborting()
    }
    case ReceiveTimeout => {
      println("timeout2")
      unbecome()
      Aborting()
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
      Aborting()
    }
  }
}
