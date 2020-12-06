package laogao.doobie

import doobie.util.transactor.Transactor
import zio._

import laogao.doobie.model._

package object repo {
  
  object Repo {
    trait Service[A] {
      def get(id: Long): Task[A]
      def create(a: A): Task[A]
      def delete(id: Long): Task[Boolean]
    }
  }

  type DbTransactor = Has[Transactor[Task]]
  type UserRepo = Has[Repo.Service[User]]

  def getUser(id: Long): RIO[UserRepo, User] = RIO.accessM(_.get.get(id))
  def createUser(a: User): RIO[UserRepo, User] = RIO.accessM(_.get.create(a))
  def deleteUser(id: Long): RIO[UserRepo, Boolean] = RIO.accessM(_.get.delete(id))

}