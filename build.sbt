ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.10"
//val NewTypeVersion = "0.4.4"
val zioVersion = "2.0.9"

lazy val root = (project in file("."))
  .settings(
    name := "ZioHell",
    libraryDependencies ++= Seq(
      "dev.zio" %% "zio" % zioVersion,
      "dev.zio" %% "zio-streams" % zioVersion,
//      "dev.zio"%% "zio-kafka" % "2.4.0",
      "dev.zio" %% "zio-json" % "0.5.0",
//      "dev.zio" %% "zio-interop-cats" % "23.0.0.0",
//      "dev.zio" %% "zio-jdbc" % "0.0.2"
      "io.d11" %% "zhttp" % "2.0.0-RC11"
    )
  )

//libraryDependencies += "org.tpolecat" %% "doobie-core" % "1.0.0-RC1"
libraryDependencies += "mysql" % "mysql-connector-java" % "8.0.33"
libraryDependencies += "org.tpolecat" %% "doobie-hikari"    % "1.0.0-RC1"
libraryDependencies += "dev.zio" %% "zio-kafka"         % "2.4.0"
libraryDependencies += "dev.zio" %% "zio-kafka-testkit" % "2.4.0" % Test
libraryDependencies += "com.influxdb" % "influxdb-client-scala_2.13" % "6.9.0"