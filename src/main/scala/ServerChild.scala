/**
 * Created by xnivel on 06.01.15.
 */
import akka.actor.{ActorRef, Actor, ReceiveTimeout}
import scala.concurrent.duration._

class ServerChild(sendedObjects:Set[(Proxy,Shared[Integer])],server: ActorRef) extends Actor {
  //val objects: Set[(Proxy,Shared[Integer])]=sendedObjects;
  context.setReceiveTimeout(Duration.Undefined)
  def unbecome() = {
    context.setReceiveTimeout(Duration.Undefined)
    context.unbecome();
  }
  def Aborting() = {
    println("send a AbortWithList")
    val Proxylist= sendedObjects.foldLeft(Set[Proxy]())((result: Set[Proxy],elem:(Proxy,Shared[Integer]))=>{
      result+elem._1
    })
    server ! new AbortWithList(Proxylist)
    context.stop(self)
  }
  def Commiting()={
    server ! new WriteCommit(sendedObjects)
    context.stop(self)
  }

  def waiting: Receive = {

    case PreCommit => {
      sender ! (Ack)
      context.become(prepared);
    }
    case Abort => {
      unbecome()
      Aborting()
    }
    case ReceiveTimeout => {
      println("timeout1")
      unbecome()
      Aborting()
    }
    case _ => {

    }
  }
  def prepared: Receive = {
    case DoCommit => {
      unbecome()
      Commiting()
    }
    case Abort => {
      unbecome()
      Aborting()
    }
    case ReceiveTimeout => {
      unbecome()
      Aborting()
    }
    case _ => {

    }
  }
  def receive = {
    case (name: String) => {
      println("received a message "+name)
    }
    case (msg:CanCommit) => {

      sender ! (Yes)
      context.setReceiveTimeout(100 milliseconds)
      context.become(waiting);
    }
    case ReceiveTimeout => {
      println("received a aaaaaaaaaaaaaaa")
    }

    case _ => println("received a messagaes")
  }
}
