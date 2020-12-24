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

object Main extends zio.App {

  val channel: ManagedChannelBuilder[_] = ManagedChannelBuilder.forAddress("localhost", 9000).usePlaintext()
  val deviceRegistryClientM = DeviceRegistryClient.managed(ZManagedChannel(channel))
  val userLoginClientM = UserLoginClient.managed(ZManagedChannel(channel))

  val program = deviceRegistryClientM.zipWith(userLoginClientM) { (deviceRegistryClient, userLoginClient) =>
    val route =
      path("api" / "device" / "register") {
        (get | post) {
          println("/api/device/register")
          val res: RegisterReply = zio.Runtime.default.unsafeRun(deviceRegistryClient.register(RegisterRequest("")))
          complete(HttpEntity(ContentTypes.`application/json`, s"""{"status":{"code":0,"desc":"0"},"result":{"_device":"${res.device}"}}"""))
        }
      } ~
        path("api" / "user" / "login") {
          (get | post) {
            println("/api/user/login")
            parameters("aid".as[Int], "wxcode".as[String], "userinfo".as[String], "ephone".as[String]) { (aid, wxcode, userinfo, ephone) =>
              println(s"aid: $aid, wxcode: $wxcode, userinfo: $userinfo, ephone: $ephone")
              optionalCookie("_device") {
                case Some(device) => {
                  val reply: WxLoginReply = zio.Runtime.default.unsafeRun(userLoginClient.wxLogin(WxLoginRequest(device.value, aid, wxcode, userinfo, ephone)))
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
            println("/api/user/me")
            cookie("_device") { device =>
              cookie("_token") { token =>
                val reply: MeReply = zio.Runtime.default.unsafeRun(userLoginClient.me(MeRequest(device.value, token.value)))
                val res = if (reply.status >= 0) s"""{"status":{"code":${reply.status},"desc":""},"result":{"uid":"${reply.uid}"}}""" else s"""{"status":{"code":${reply.status},"desc":""}}"""
                complete(HttpEntity(ContentTypes.`application/json`, res))
              }
            }
          }
        } ~
        path("api" / "demo") {
          (get | post) {
            println("/api/demo")
            complete(HttpEntity(ContentTypes.`application/json`, """{"staus":{"code":0}}"""))
          }
        }
    implicit val system = ActorSystem(Behaviors.empty, "http-endpoint")
    Http().newServerAt("localhost", 8080).bind(route)
  }.useForever

  final def run(args: List[String]) =
    program.exitCode

}