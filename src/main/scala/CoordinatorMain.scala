import akka.actor.{Props, ActorSystem}
import com.typesafe.config.ConfigFactory

object CoordinatorMain extends App {
  val port = args(0)

  val config = ConfigFactory.
    parseString(s"akka.remote.netty.tcp.port = $port").
    withFallback(ConfigFactory.load()).resolve()
  val system = ActorSystem("CoordinatorSystem", config)
  system.actorOf(Props[Coordinator], "coordinator")
}
