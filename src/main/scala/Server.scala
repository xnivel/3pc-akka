import akka.actor.{Props, Actor, ReceiveTimeout}
import scala.concurrent.duration._

class Server(var objects: Map[String, (Shared[Integer],Boolean)]) extends Actor {
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
//    case (msg: Write) => {
//      objects = objects.updated(msg.id, new Shared[Integer](msg.newVal,1));
//    }
    case (msg: WriteCommit) => {
      println("received a docommit")
//      objects = objects.updated(msg.id, new Shared[Integer](msg.newVal,1));
    }
    case (msg: CanCommit) => {
      val needToAbort = msg.objects.foldLeft(false)((result: Boolean,elem:(Proxy,Shared[Integer])) => {
      val idOfVariable=elem._1.variableId
      if(result!=true&&objects.contains(idOfVariable))
      {
        if((!objects(idOfVariable)._2)&&(objects(idOfVariable)._1.version<elem._2.version))
          false
        else
          true
      }
      else
        result
    })
      if(!needToAbort){
        objects = msg.objects.foldLeft(objects)((result: Map[String, (Shared[Integer],Boolean)],elem:(Proxy,Shared[Integer]))=>{
          val idOfVariable=elem._1.variableId
          if(objects.contains(idOfVariable))
            result.updated(idOfVariable,(result(idOfVariable)._1,true))
          else
            result
        })
        val child = context.actorOf(Props(classOf[ServerChild], ""))
        child forward CanCommit
      }else{
        sender ! new No
      }
    }
    case ReceiveTimeout => {
      println("received a aaaaaaaaaaaaaaa")
    }

    case _ => println("received a messagae")
  }
}
