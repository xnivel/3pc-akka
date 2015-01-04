abstract class Message
case class Read(id: String) extends Message
case class CommitRequest(buffer: Map[Proxy, Integer]) extends Message
case class Commit() extends Message
case class Abort() extends Message