package com.hootsuite.akkak8s

import com.typesafe.config.ConfigFactory
import akka.actor.{ActorSystem, CoordinatedShutdown, Props}
import akka.cluster.Cluster
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import akka.util.Timeout
import akka.http.scaladsl.server.Directives._

import scala.concurrent.duration._
import akka.pattern.ask
import akka.routing.FromConfig

import scala.concurrent.Future

object SimpleClusterApp extends App {

  // Initialize config. This node's roles can be overridden by the CLUSTER_ROLES env var
  val baseConfig = ConfigFactory.load()
  val overrideConfig = sys.env.get("CLUSTER_ROLES").map(roles => s"akka.cluster.roles = [$roles]").getOrElse("")
  val config = ConfigFactory.parseString(overrideConfig).withFallback(baseConfig)

  // akka init
  implicit val system = ActorSystem("ClusterSystem", config)
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher

  // initialize the cluster
  val cluster = Cluster(system)

  // backend just runs the backend worker
  if (cluster.selfRoles.contains("backend")) {
    system.actorOf(Props[Backend], name = "backend")
  }

  // frontend runs frontend actors and akka-http
  if (cluster.selfRoles.contains("frontend")) {

    // start actors
    val backendRouter = system.actorOf(
      FromConfig.props(Props.empty),
      name = "backendRouter")
    val frontend = system.actorOf(Frontend.props(backendRouter), name = "frontend")

    // create HTTP routes
    val route =
      path("") {
        get {
          parameter("msg") { (message) =>
            implicit val timeout: Timeout = 1.second
            val response: Future[NotHotDogResponse] = (frontend ? NotHotDogRequest(message)).mapTo[NotHotDogResponse]
            complete(response.map(r => if (r.hotDog) "Hot Dog" else "Not Hot Dog"))
          }
        } ~
          // health check endpoint
          path("health") {
            get {
              complete("OK")
            }
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

final case class NotHotDogResponse(hotDog: Boolean)

final case class NotHotDogRequest(message: String)
