name := "reliable-coordinator-v2"

version := "0.1"

scalaVersion := "2.12.4"

libraryDependencies ++= {
  val akka = "com.typesafe.akka"
  val akkaV = "2.5.7"

  Seq(
    akka             %% "akka-actor"                 % akkaV,
    akka             %% "akka-testkit"               % akkaV % Test,
    akka             %% "akka-persistence"           % akkaV,
    akka             %% "akka-slf4j"                 % akkaV,
    akka             %% "akka-persistence-cassandra" % "0.58",
    "ch.qos.logback" % "logback-classic"             % "1.2.3"
  )
}
