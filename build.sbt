name := "stream-labs"

version := "0.1"

scalaVersion := "2.11.8"

resolvers ++= Seq(
  Resolver.sonatypeRepo("releases"),
  Resolver.sonatypeRepo("snapshots")
)
libraryDependencies ++= {
  val akkaV = "2.4.17"
  val akkaHttpV = "10.0.9"
  val slickV = "3.1.0"
  Seq(
    "com.typesafe.akka"   %%  "akka-http"    % akkaHttpV,
    "com.typesafe.akka"   %%  "akka-http-core"    % akkaHttpV,
    "com.typesafe.akka"   %%  "akka-http-testkit" % akkaHttpV,
    "com.typesafe.akka"	  %%  "akka-http-spray-json"	% akkaHttpV,
    "com.typesafe.akka"   %%  "akka-actor"    % akkaV,
    "ch.megard" %% "akka-http-cors" % "0.2.1",

    "com.github.swagger-akka-http" %% "swagger-akka-http" % "0.11.0",

    "com.typesafe.slick" %% "slick" % slickV,
    "org.slf4j" % "slf4j-nop" % "1.6.4",
    "com.typesafe.slick" %% "slick-hikaricp" % slickV,
    "com.byteslounge" %% "slick-repo" % "1.4.3",

    "org.xerial" % "sqlite-jdbc" % "3.7.2"
  )
}