import akka.actor.{Props, Actor, ReceiveTimeout}
import scala.concurrent.duration._

class Server extends Actor {
  var objects = Map[String, (Shared[Integer],Boolean)]().withDefaultValue((new Shared[Integer](0,0),false))
  val name= context.self.path.toString
  def receive = {
    case (name: String) => {
      println("received a message "+name)
    }
    case (msg: Read) => {
      sender ! (objects(msg.id)._1)
    }
    case (msg: WriteCommit) => {
      def WriteChanges: (Map[String, (Shared[Integer], Boolean)], (Proxy, Shared[Integer])) => Map[String, (Shared[Integer], Boolean)] = {
        (result: Map[String, (Shared[Integer], Boolean)], elem: (Proxy, Shared[Integer])) => {
          val idOfVariable = elem._1.variableId
          val nameOfServerVariable = elem._1.serverId
          if (name == nameOfServerVariable && objects.contains(idOfVariable))
            result.updated(idOfVariable, (elem._2, false))
          else
            result
        }
      }
      objects = msg.objects.foldLeft(objects)(WriteChanges)
    }
    case (msg: AbortWithList) => {
      println("received a AbortWithList")
      def AbortCommit: (Map[String, (Shared[Integer], Boolean)], (Proxy, Shared[Integer])) => Map[String, (Shared[Integer], Boolean)] = {
        (result: Map[String, (Shared[Integer], Boolean)], elem: (Proxy, Shared[Integer])) => {
          val idOfVariable = elem._1.variableId
          val nameOfServerVariable = elem._1.serverId
          if (name == nameOfServerVariable && objects.contains(idOfVariable))
            result.updated(idOfVariable, (result(idOfVariable)._1, false))
          else
            result
        }
      }
      objects = msg.objects.foldLeft(objects)(AbortCommit)
    }
    case (msg: CanCommit) => {
      def conflicts: ((Proxy, Shared[Integer])) => Boolean = {
        elem => {
          val idOfVariable = elem._1.variableId
          val nameOfServerVariable = elem._1.serverId
          if (name == nameOfServerVariable && objects.contains(idOfVariable)) {
            !((!objects(idOfVariable)._2) && (objects(idOfVariable)._1.version < elem._2.version))
          }
          else
            false
        }
      }
      val needToAbort = msg.objects.exists(conflicts)
      if(!needToAbort){
        objects = msg.objects.foldLeft(objects)((result: Map[String, (Shared[Integer],Boolean)],elem:(Proxy,Shared[Integer]))=>{
          val idOfVariable=elem._1.variableId
          if(objects.contains(idOfVariable))
            result.updated(idOfVariable,(result(idOfVariable)._1,true))
          else
            result
        })
        val child = context.actorOf(Props(new ServerChild(msg.objects,context.self)))
        child forward msg
      }else{
        sender ! No()
      }
    }

    case _ => println("received a messagae")
  }
}
