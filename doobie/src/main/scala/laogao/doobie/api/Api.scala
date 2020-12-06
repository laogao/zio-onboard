package laogao.doobie.api

import io.circe.generic.auto._
import io.circe.{ Decoder, Encoder }
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import org.http4s.{ EntityDecoder, EntityEncoder, HttpRoutes }
import zio._
import zio.interop.catz._

import laogao.doobie.model._
import laogao.doobie.repo._

final case class Api[R <: UserRepo](rootUri: String) {

  type UserTask[A] = RIO[R, A]

  implicit def circeJsonDecoder[A](implicit decoder: Decoder[A]): EntityDecoder[UserTask, A] = jsonOf[UserTask, A]
  implicit def circeJsonEncoder[A](implicit decoder: Encoder[A]): EntityEncoder[UserTask, A] = jsonEncoderOf[UserTask, A]

  val dsl: Http4sDsl[UserTask] = Http4sDsl[UserTask]
  import dsl._

  def route: HttpRoutes[UserTask] = {

    HttpRoutes.of[UserTask] {
      case GET -> Root / LongVar(id) => getUser(id).foldM(_ => NotFound(), Ok(_))
      case request @ POST -> Root =>
        request.decode[User] { user =>
          Created(createUser(user))
        }
      case DELETE -> Root / LongVar(id) =>
        (getUser(id) *> deleteUser(id)).foldM(_ => NotFound(), Ok(_))
    }
  }

}