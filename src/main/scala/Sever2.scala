/**
 * Created by xnivel on 09.01.15.
 */
import akka.actor._

object Sever2 extends App  {
   val system = ActorSystem("RemoteActorSystem")
   val map=Map("d"->((new Shared[Integer](1,0)),false));
   system.actorOf(Props(new Server(map)), "Server2") // Creates actor instance.
 }
