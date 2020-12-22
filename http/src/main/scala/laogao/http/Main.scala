package laogao.http

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import scala.io.StdIn

object Main extends App {

  implicit val system = ActorSystem(Behaviors.empty, "http-endpoint")
  implicit val executionContext = system.executionContext

  val route =
    path("api" / "device" / "register") {
      (get | post) {
        complete(HttpEntity(ContentTypes.`application/json`, """{"status":{"code":0,"desc":"0"},"result":{"_device":"123"}}"""))
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
    }

  val bindingFuture = Http().newServerAt("localhost", 8080).bind(route)
  println(s"Server online at http://localhost:8080/\nPress RETURN to stop...")
  StdIn.readLine() // let it run until user presses return
  bindingFuture
    .flatMap(_.unbind()) // trigger unbinding from the port
    .onComplete(_ => system.terminate()) // and shutdown when done

}