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
      objects = msg.objects.foldLeft(objects)((result: Map[String, (Shared[Integer],Boolean)],elem:(Proxy,Shared[Integer]))=>{
        val idOfVariable=elem._1.variableId
        val nameOfServerVariable=elem._1.serverId
        if(name==nameOfServerVariable&&objects.contains(idOfVariable))
          result.updated(idOfVariable,(elem._2,false))
        else
          result
      })
    }
    case (msg: AbortWithList) => {
      println("received a AbortWithList")
      objects = msg.objects.foldLeft(objects)((result: Map[String, (Shared[Integer],Boolean)],elem:(Proxy,Shared[Integer])) => {
        val idOfVariable=elem._1.variableId
        val nameOfServerVariable=elem._1.serverId
        if(name==nameOfServerVariable&&objects.contains(idOfVariable))
          result.updated(idOfVariable,(result(idOfVariable)._1,false))
        else
          result
      })
    }
    case (msg: CanCommit) => {
      /*val needToAbort = msg.objects.foldLeft(false)((result: Boolean,elem:(Proxy,Shared[Integer])) => {
      val idOfVariable=elem._1.variableId
      val nameOfServerVariable=elem._1.serverId
      if(result!=true&&name==nameOfServerVariable&&objects.contains(idOfVariable))
      {
        !((!objects(idOfVariable)._2)&&(objects(idOfVariable)._1.version<elem._2.version))
      }
      else
        result
    })*/
      val needToAbort = msg.objects.exists(elem =>{
        val idOfVariable=elem._1.variableId
        val nameOfServerVariable=elem._1.serverId
        if(name==nameOfServerVariable&&objects.contains(idOfVariable))
        {
          !((!objects(idOfVariable)._2)&&(objects(idOfVariable)._1.version<elem._2.version))
        }
        else
          false
      })
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
