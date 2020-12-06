package laogao.doobie

package object model {
  final case class User(id: Long, name: String)
  final case class UserNotFound(id: Long) extends Exception
}