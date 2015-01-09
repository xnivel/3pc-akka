/**
 * Created by xnivel on 09.01.15.
 */
package remoting

import akka.actor._

class RemoteActor extends Actor {
  def receive = {
    case _ =>
      println("received 123" )
        sender ! (1)
  }
}


object Sever1 extends App  {
  println("ser1 start")
  val system = ActorSystem("RemoteActorSystem")
  //val map=Map("c"->((new Shared[Integer](2,0)),false));
  system.actorOf(Props(new RemoteActor), "Server1") // Creates actor instance.
}
