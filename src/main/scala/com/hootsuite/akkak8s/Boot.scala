package com.hootsuite.akkak8s

import java.net.InetAddress

import akka.actor.{ActorSystem, CoordinatedShutdown, Props}
import akka.cluster.Cluster
import akka.cluster.routing.{ClusterRouterGroup, ClusterRouterGroupSettings}
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.pattern.ask
import akka.routing.RoundRobinGroup
import akka.stream.ActorMaterializer
import akka.util.Timeout
import com.typesafe.config.ConfigFactory

import scala.concurrent.Future
import scala.concurrent.duration._

object SimpleClusterApp extends App {

  // akka init
  val baseConfig = ConfigFactory.load()
  val overrideConfig = sys.env.get("CLUSTER_ROLES").map(roles => s"akka.cluster.roles = [$roles]").getOrElse("")
  val config = ConfigFactory.parseString(overrideConfig).withFallback(baseConfig)

  implicit val system = ActorSystem("ClusterSystem", config)
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher

  // initialize the cluster
  val cluster = Cluster(system)

  if (cluster.selfRoles.contains("backend")) {
    system.actorOf(Props[Backend], name = "backend")
  }

  if (cluster.selfRoles.contains("frontend")) {
    val backendRouter = system.actorOf(
      ClusterRouterGroup(
        RoundRobinGroup(Nil),
        ClusterRouterGroupSettings(
          totalInstances = 1000,
          routeesPaths = List("/user/backend"),
          allowLocalRoutees = true,
          useRole = Some("backend")
        )
      ).props(),
      name = "backendRouter")

    val frontend = system.actorOf(Frontend.props(backendRouter), name = "frontend")
    val hostname = InetAddress.getLocalHost.getHostName

    // create HTTP routes
    val route =
      path("") {
        get {
          parameter("msg") { (message) =>
            implicit val timeout: Timeout = 1.second
            val response: Future[NotHotDogResponse] = (frontend ? NotHotDogRequest(message)).mapTo[NotHotDogResponse]
            complete(response.map(r => if (r.hotDog) s"Hot Dog! (from fe: ${hostname} be: ${r.src})" else s"Not Hot Dog :( (from fe: ${hostname} be: ${r.src})"))
          }
        }
      } ~ path("health") {
        get {
          complete("OK")
        }
      }

    // start server
    val bindingFuture = Http().bindAndHandle(route, "0.0.0.0", 8080)

    // ensure akka-http shuts down cleanly
    CoordinatedShutdown(system).addJvmShutdownHook({
      bindingFuture
        .flatMap(_.unbind())
    })
  }
}

final case class NotHotDogResponse(hotDog: Boolean, src: String)

final case class NotHotDogRequest(message: String)
