abstract class Message
case class Read(id: String) extends Message
case class Write(id: String,newVal: Integer) extends Message
case class CommitRequest(buffer: Map[Proxy, Integer]) extends Message
case class Commit() extends Message
case class Abort() extends Message
case class CanCommit(objects: Set[Proxy])

case class PreCommit() extends Message
case class DoCommit() extends Message