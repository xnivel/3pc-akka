abstract class Message
case class Read(id: String) extends Message
case class Write(id: String,newVal: Int) extends Message
case class CommitRequest(objects: Map[VarRef, Shared[Int]]) extends Message
case class Commit() extends Message
case class Abort() extends Message
case class AbortWithList(objects: Set[(VarRef, Shared[Int])]) extends Message
case class CanCommit(objects: Set[(VarRef, Shared[Int])]) extends Message
case class PreCommit() extends Message
case class DoCommit() extends Message
case class Yes() extends Message
case class No() extends Message
case class Ack() extends Message
case class WriteCommit(objects: Set[(VarRef, Shared[Int])]) extends Message
