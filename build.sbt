name := "akka-persistence"

version := "0.1"

scalaVersion := "2.13.3"

val AkkaVersion = "2.6.10"
libraryDependencies ++= Seq(
  "com.typesafe.akka"         %% "akka-persistence-typed"   % AkkaVersion,
  "com.typesafe.akka"         %% "akka-persistence-testkit" % AkkaVersion % Test,
  "io.altoo"                  %% "akka-kryo-serialization"  % "1.1.5",
  "org.iq80.leveldb"          % "leveldb"                   % "0.12",
  "org.fusesource.leveldbjni" % "leveldbjni-all"            % "1.8",
  "org.scalactic"             %% "scalactic"                % "3.2.0",
  "org.scalatest"             %% "scalatest"                % "3.2.0" % Test,
  "commons-io"                % "commons-io"                % "2.6",
  "ch.qos.logback"            % "logback-classic"           % "1.1.3"
)

parallelExecution in Test := false

fork := true
