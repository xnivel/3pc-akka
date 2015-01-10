import akka.actor.{Props, ActorSystem}
import com.typesafe.config.ConfigFactory

object ServerMain extends App {
  val name = args(0)
  val port = args(1)

  val config = ConfigFactory.
    parseString(s"akka.remote.netty.tcp.port = $port").
    withFallback(ConfigFactory.load())
  val system = ActorSystem("ServerSystem", config)
  system.actorOf(Props[Server], name)
}
