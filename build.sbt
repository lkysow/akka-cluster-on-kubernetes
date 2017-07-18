name := "akka-cluster-on-kubernetes"

version := "0.1"

scalaVersion := "2.12.2"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % "2.5.3",
  "com.typesafe.akka" %% "akka-cluster" % "2.5.3",
  "com.typesafe.akka" %% "akka-http" % "10.0.9",
  "com.typesafe.akka" %% "akka-http-spray-json" % "10.0.9",
  "de.heikoseeberger" %% "constructr" % "0.17.0",
  "de.heikoseeberger" %% "constructr-coordination-etcd" % "0.17.0"
)
