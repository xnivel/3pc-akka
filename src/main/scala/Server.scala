import akka.actor.{Props, Actor}

class Server extends Actor {
  var objects = Map[String, (Shared[Int], Boolean)]().withDefaultValue((new Shared[Int](0, 0), false))
  val name = context.self.path.toString

  def receive = {
    case (name: String) => {
      println("received a message "+name)
    }
    case (msg: Read) => {
      sender ! objects(msg.id)._1
    }
    case (msg: WriteCommit) => {
      println("received a WriteCommit "+self.path.name)
      def writeChanges: (Map[String, (Shared[Int], Boolean)], (VarRef, Shared[Int])) => Map[String, (Shared[Int], Boolean)] = {
        (result: Map[String, (Shared[Int], Boolean)], elem: (VarRef, Shared[Int])) => {
          val idOfVariable = elem._1.variableId
          val nameOfServerVariable = elem._1.serverId
          if (name == nameOfServerVariable)
            result.updated(idOfVariable, (elem._2, false))
          else
            result
        }
      }
      objects = msg.objects.foldLeft(objects)(writeChanges)
      println("end a WriteCommit "+self.path.name)
    }
    case (msg: AbortWithList) => {
      println("received a AbortWithList")
      def abortCommit: (Map[String, (Shared[Int], Boolean)], (VarRef, Shared[Int])) => Map[String, (Shared[Int], Boolean)] = {
        (result: Map[String, (Shared[Int], Boolean)], elem: (VarRef, Shared[Int])) => {
          val idOfVariable = elem._1.variableId
          val nameOfServerVariable = elem._1.serverId
          if (name == nameOfServerVariable)
            result.updated(idOfVariable, (result(idOfVariable)._1, false))
          else
            result
        }
      }
      objects = msg.objects.foldLeft(objects)(abortCommit)
    }
    case (msg: CanCommit) => {
      def conflicts: ((VarRef, Shared[Int])) => Boolean = {
        elem => {
          val idOfVariable = elem._1.variableId
          val nameOfServerVariable = elem._1.serverId
          if (name == nameOfServerVariable) {
            !((!objects(idOfVariable)._2) && (objects(idOfVariable)._1.version < elem._2.version))
          }
          else
            false
        }
      }
      val needToAbort = msg.objects.exists(conflicts)
      if (!needToAbort) {
        objects = msg.objects.foldLeft(objects)((result: Map[String, (Shared[Int],  Boolean)], elem:(VarRef, Shared[Int])) => {
          val idOfVariable=elem._1.variableId
          val nameOfServerVariable = elem._1.serverId
          if (name == nameOfServerVariable)
            result.updated(idOfVariable, (result(idOfVariable)._1, true))
          else
            result
        })
        println("yes z serwera "+self.path.name)
        val child = context.actorOf(Props(new ServerChild(msg.objects, context.self)))
        child forward msg
      } else {
        println("no z serwera "+self.path.name)
        sender ! No()
      }
    }
    case _ => println("received a messagae")
  }
}
