package laogao.doobie.repo

import scala.concurrent.ExecutionContext

import cats.effect.Blocker
import laogao.doobie.configuration
import laogao.doobie.configuration.DbConfig
import laogao.doobie.model._
import doobie.h2.H2Transactor
import doobie.implicits._
import doobie.{ Query0, Transactor, Update0 }
import zio._
import zio.blocking.Blocking
import zio.interop.catz._

final class UserRepoService(tnx: Transactor[Task]) extends Repo.Service[User] {
  import UserRepoService._ // TODO

  def get(id: Long): Task[User] =
    SQL
      .get(id)
      .option
      .transact(tnx)
      .foldM(
        err => Task.fail(err),
        maybeUser => Task.require(UserNotFound(id))(Task.succeed(maybeUser))
      )

  def create(user: User): Task[User] =
    SQL
      .create(user)
      .run
      .transact(tnx)
      .foldM(err => Task.fail(err), _ => Task.succeed(user))

  def delete(id: Long): Task[Boolean] =
    SQL
      .delete(id)
      .run
      .transact(tnx)
      .fold(_ => false, _ => true)
}

object UserRepoService {

  object SQL {

    def get(id: Long): Query0[User] =
      sql"""SELECT * FROM USERS WHERE ID = $id """.query[User]

    def create(user: User): Update0 =
      sql"""INSERT INTO USERS (id, name) VALUES (${user.id}, ${user.name})""".update

    def delete(id: Long): Update0 =
      sql"""DELETE FROM USERS WHERE id = $id""".update

    def createUsersTable: doobie.Update0 =
      sql"""
        CREATE TABLE USERS (
          id   INT,
          name VARCHAR NOT NULL
        )
        """.update
  }

  def createUserTable: ZIO[DbTransactor, Throwable, Unit] =
    for {
      tnx <- ZIO.service[Transactor[Task]]
      _ <-
        SQL.createUsersTable.run
          .transact(tnx)
    } yield ()

  def mkTransactor(
      conf: DbConfig,
      connectEC: ExecutionContext,
      transactEC: ExecutionContext
  ): Managed[Throwable, Transactor[Task]] = {
    import zio.interop.catz._

    H2Transactor
      .newH2Transactor[Task](
        conf.url,
        conf.user,
        conf.password,
        connectEC,
        Blocker.liftExecutionContext(transactEC)
      )
      .toManagedZIO
  }

  val transactorLive: ZLayer[Has[DbConfig] with Blocking, Throwable, DbTransactor] =
    ZLayer.fromManaged(for {
      config     <- configuration.dbConfig.toManaged_
      connectEC  <- ZIO.descriptor.map(_.executor.asEC).toManaged_
      blockingEC <- blocking.blocking { ZIO.descriptor.map(_.executor.asEC) }.toManaged_
      transactor <- mkTransactor(config, connectEC, blockingEC)
    } yield transactor)

  val live: ZLayer[DbTransactor, Throwable, UserRepo] =
    ZLayer.fromService(new UserRepoService(_))

}