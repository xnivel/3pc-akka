import akka.actor.{Props, ActorSystem}
import com.typesafe.config.ConfigFactory

object CoordinatorMain extends App {
  val hostname = args(0)
  val port = args(1)

  val config = ConfigFactory.
    parseString(
      s"""
         akka.remote.netty.tcp {
           hostname = $hostname
           port = $port
         }""").
    withFallback(ConfigFactory.load()).resolve()
  val system = ActorSystem("CoordinatorSystem", config)
  system.actorOf(Props[Coordinator], "coordinator")
}
