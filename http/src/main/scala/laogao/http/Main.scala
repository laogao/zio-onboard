package laogao.http

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import io.grpc.ManagedChannelBuilder
import laogao.grpc.user.{MeReply, MeRequest, RegisterReply, RegisterRequest, WxLoginReply, WxLoginRequest}
import laogao.grpc.user.ZioUser.{DeviceRegistryClient, UserLoginClient}
import scalapb.zio_grpc.ZManagedChannel

import scala.io.StdIn

object Main extends App {

  implicit val system = ActorSystem(Behaviors.empty, "http-endpoint")
  implicit val executionContext = system.executionContext

  val channel: ManagedChannelBuilder[_] = ManagedChannelBuilder.forAddress("localhost", 9000).usePlaintext()
  val deviceRegistryClient = DeviceRegistryClient.managed(ZManagedChannel(channel))
  val userLoginClient = UserLoginClient.managed(ZManagedChannel(channel))

  val route =
    path("api" / "device" / "register") {
      (get | post) {
        val res: RegisterReply = zio.Runtime.default.unsafeRun(deviceRegistryClient.use(_.register(RegisterRequest(""))))
        complete(HttpEntity(ContentTypes.`application/json`, s"""{"status":{"code":0,"desc":"0"},"result":{"_device":"${res.device}"}}"""))
      }
    } ~ 
    path("api" / "user" / "login") {
      (get | post) {
        parameters("aid".as[Int], "wxcode".as[String], "userinfo".as[String], "ephone".as[String]) { (aid, wxcode, userinfo, ephone) =>
          println(s"aid: $aid, wxcode: $wxcode, userinfo: $userinfo, ephone: $ephone")
          optionalCookie("_device") {
            case Some(device) => {
              val reply: WxLoginReply = zio.Runtime.default.unsafeRun(userLoginClient.use(_.wxLogin(WxLoginRequest(device.value, aid, wxcode, userinfo, ephone))))
              val res = if (reply.status >= 0) s"""{"status":{"code":${reply.status},"desc":""},"result":{"_token":"${reply.token}"}}""" else s"""{"status":{"code":${reply.status},"desc":""}}"""
              complete(HttpEntity(ContentTypes.`application/json`, res))
            }
            case None => complete(HttpEntity(ContentTypes.`application/json`, """{"status":{"code":-200,"desc":"无效的设备"}}"""))
          }
        }
      }
    } ~ 
    path("api" / "user" / "me") {
      (get | post) {
        cookie("_device") { device =>
          cookie("_token") { token =>
            val reply: MeReply = zio.Runtime.default.unsafeRun(userLoginClient.use(_.me(MeRequest(device.value, token.value))))
            val res = if (reply.status >= 0) s"""{"status":{"code":${reply.status},"desc":""},"result":{"uid":"${reply.uid}"}}""" else s"""{"status":{"code":${reply.status},"desc":""}}"""
            complete(HttpEntity(ContentTypes.`application/json`, res))
          }
        }
      }
    }

  val bindingFuture = Http().newServerAt("localhost", 8080).bind(route)
  println(s"Server online at http://localhost:8080/\nPress RETURN to stop...")
  StdIn.readLine() // let it run until user presses return
  bindingFuture
    .flatMap(_.unbind()) // trigger unbinding from the port
    .onComplete(_ => system.terminate()) // and shutdown when done

}