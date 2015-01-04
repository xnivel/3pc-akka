import akka.actor.Actor

class Server(var objects: Map[String, Integer]) extends Actor {
  def receive = {
    case (name: String) => {
      println("received a message "+name)
    }
    case (command: String, variablename: String) => {
//      println("received a command "+command+ " variablename "+variablename)
      command match {
        case "read" => {
//          println("r"+ (objects get variablename get))
          sender ! (objects get variablename get)
        }
        case _ =>{}
      }
    }
    case (command: String, variablename: String,variable: Integer) => {
//      println("received a command "+command+ " variablename "+variablename+" variablevalue "+variable)
      command match {
        case "write" => {
          objects = objects.updated(variablename, variable)
        }
        case _ =>{}
      }
    }
    case _ => println("received a message")
  }
}
