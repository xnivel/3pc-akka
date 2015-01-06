import akka.actor.{Props, Actor, ReceiveTimeout}
import scala.concurrent.duration._

class Server(var objects: Map[String, Shared[Integer]]) extends Actor {
  def receive = {
    case (name: String) => {
      println("received a message "+name)
    }
    case (msg: Read) => {
      sender ! (objects get msg.id get)
    }
//      Raczej i tak nie powinna taka wiadomosc docierac
//      czysto do testow
//      obecnie nic ciekawego
    case (msg: Write) => {
      objects = objects.updated(msg.id, new Shared[Integer](msg.newVal,1));
    }
    case WriteCommit => {
      println("received a docommit")
    }
    case CanCommit => {
//      jesli wszystkie zmienne false
//      new aktor send yes
//      else
//      send no

      val child = context.actorOf(Props(classOf[ServerChild], ""))
      child forward CanCommit
    }
    case ReceiveTimeout => {
      println("received a aaaaaaaaaaaaaaa")
    }

    case _ => println("received a messagae")
  }
}
