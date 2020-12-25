package laogao.http

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule

import scala.io.StdIn

object JsonDemo extends App {

  implicit val system = ActorSystem(Behaviors.empty, "http-endpoint")
  implicit val executionContext = system.executionContext

  case class Status(code: Int, desc: String)
  case class DeviceResult(_device: String)
  case class LoginResult(_token: String)
  case class MeResult(uid: Long, nick: String)

  case class DeviceResponse(status: Status, result: DeviceResult)
  case class LoginResponse(status: Status, result: LoginResult)
  case class MeResponse(status: Status, result: MeResult)

  val mapper = JsonMapper.builder().addModule(DefaultScalaModule).build()
  mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

  val json1 = """{"status":{"code":0,"desc":""},"result":{"_device":"123"}}"""
  val data1 = mapper.readValue(json1, classOf[DeviceResponse])
  println(data1)
  println(mapper.writeValueAsString(data1))

  val json2 = """{"status":{"code":0,"desc":""},"result":{"_token":"789"}}"""
  val data2 = mapper.readValue(json2, classOf[LoginResponse])
  println(data2)
  println(mapper.writeValueAsString(data2))

  val json3 = """{"status":{"code":0,"desc":""},"result":{"uid":50001,"nick":"laogao"}}"""
  val data3 = mapper.readValue(json3, classOf[MeResponse])
  println(data3)
  println(mapper.writeValueAsString(data3))

  val json4 = """{"status":{"code":0,"desc":""}}"""
  val data4 = mapper.readValue(json4, classOf[MeResponse])
  println(data4)
  println(mapper.writeValueAsString(data4))

  println(mapper.writeValueAsString(Map(0 -> Status(0,""), -100 -> Status(-100,"INVALID"))))
  println(mapper.writeValueAsString(Map(0 -> List(1, 2, 3), -100 -> Status(-100,"INVALID"))))
  println(mapper.readValue(s"""{"code": 0, "desc": "", "unknown": ""}""", classOf[Status]))

  val route =
    path("api" / "device" / "register") {
      (get | post) {
        complete(HttpEntity(ContentTypes.`application/json`, """{"status":{"code":0,"desc":"0"},"result":{"_device":"123"}}"""))
      }
    } ~
    path("api" / "device" / "register2") {
      (get | post) {
        complete(HttpEntity(ContentTypes.`application/json`, mapper.writeValueAsString(DeviceResponse(Status(0,""),DeviceResult("123")))))
      }
    } ~
    path("api" / "user" / "login") {
      (get | post) {
        parameters("aid".as[Int], "wxcode".as[String], "userinfo".as[String], "ephone".as[String]) { (aid, wxcode, userinfo, ephone) =>
          println(s"aid: $aid, wxcode: $wxcode, userinfo: $userinfo, ephone: $ephone")
          optionalCookie("_device") {
            case Some(device) => complete(HttpEntity(ContentTypes.`application/json`, """{"status":{"code":0,"desc":"0"},"result":{"_token":"789"}}"""))
            case None         => complete(HttpEntity(ContentTypes.`application/json`, """{"status":{"code":-200,"desc":"无效的设备"}}"""))
          }
        }
      }
    } ~
    path("api" / "user" / "me") {
      (get | post) {
        optionalCookie("_token") {
          case Some(token) => complete(HttpEntity(ContentTypes.`application/json`, """{"status":{"code":0,"desc":"0"},"result":{"uid":50001,"nick":"laogao"}}"""))
          case None        => complete(HttpEntity(ContentTypes.`application/json`, """{"status":{"code":-300,"desc":"无效的登录"}}"""))
        }
      }
    } ~
    path("api" / "post") {
      post {
        entity(as[String]) { payload => 
          complete(s"received: $payload")
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