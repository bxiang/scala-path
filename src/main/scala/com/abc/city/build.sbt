enablePlugins(JavaAppPackaging)

organization := "snap"
name := "monitoring"
ThisBuild / scalaVersion := "2.13.6"

version := sys.props.get("build.number").getOrElse("DEV-BUILD")

lazy val Http4sVersion = Http4sVersionNewer
val LogbackVersion     = "1.2.5"
val TsecVersion        = "0.4.0"

libraryDependencies ++= Seq(
  "org.http4s"           %% "http4s-blaze-server"      % Http4sVersion,
  "org.http4s"           %% "http4s-blaze-client"      % Http4sVersion,
  "org.http4s"           %% "http4s-dsl"               % Http4sVersion,
  "org.http4s"           %% "http4s-scalatags"         % Http4sVersion,
  "org.http4s"           %% "http4s-circe"             % Http4sVersion,
  "io.github.jmcardon"   %% "tsec-http4s"              % TsecVersion,
  "io.github.jmcardon"   %% "tsec-jwt-sig"             % TsecVersion,
  "io.circe"             %% "circe-generic"            % "0.14.1",
  "io.circe"             %% "circe-generic-extras"     % "0.14.1",
  "io.circe"             %% "circe-derivation"         % "0.13.0-M5",
  "io.circe"             %% "circe-optics"             % "0.14.1",
  "ch.qos.logback"       % "logback-classic"           % LogbackVersion,
  "net.logstash.logback" % "logstash-logback-encoder"  % "6.6",
  "org.mongodb.scala"    %% "mongo-scala-driver"       % "4.3.0",
  "org.mongodb"          % "mongodb-driver-core"       % "4.3.0",
  "org.mongodb"          % "mongodb-driver-sync"       % "4.3.0",
  "io.netty"             % "netty-all"                 % "4.1.67.Final",
  "org.typelevel"        %% "kittens"                  % "2.3.2",
  "org.scalatest"        %% "scalatest"                % "3.2.9" % Test,
  "com.eed3si9n.expecty" %% "expecty"                  % "0.15.4" % Test,
  "org.jxls"             % "jxls-poi"                  % "2.10.0",
  "io.github.kirill5k"   %% "mongo4cats-core"          % "0.4.4",
  "io.github.kirill5k"   %% "mongo4cats-circe"         % "0.4.4",
  "io.github.kirill5k"   %% "mongo4cats-embedded"      % "0.4.4",
  "javax.mail"           % "mail"                      % "1.5.0-b01"
)

reStart := reStart
  .dependsOn((Test / testQuick).toTask(""))
  .dependsOn((testassist / Test / testQuick).toTask(""))
  .dependsOn((shared.jvm / Test / testQuick).toTask(""))
  .dependsOn((frontend / Test / testQuick).toTask(""))
  .evaluated

reStart / envVars += "HTTP_PORT" -> "8081"

Compile / resourceGenerators += Def.task {
  val base = (Compile / resourceManaged).value
  (frontend / Compile / fastOptJS / webpack).value.map { f =>
    val tgtFile = base / "assets" / f.data.getName
    IO.copyFile(f.data, tgtFile)
    tgtFile
  }.toList
}.taskValue

addCompilerPlugin("com.olegpy" %% "better-monadic-for" % "0.3.1")

scalacOptions ++= Seq(
  "-deprecation",
  "-encoding",
  "utf-8",
  "-feature",
  "-language:existentials",
  "-language:experimental.macros",
  "-language:higherKinds",
  "-language:implicitConversions",
  "-unchecked",
  "-Xcheckinit",
  "-Xfatal-warnings",
  "-Xlint:adapted-args",
  "-Xlint:constant",
  "-Xlint:delayedinit-select",
  "-Xlint:doc-detached",
  "-Xlint:inaccessible",
  "-Xlint:missing-interpolator",
  "-Xlint:nullary-unit",
  "-Xlint:option-implicit",
  "-Xlint:package-object-classes",
  "-Xlint:poly-implicit-overload",
  "-Xlint:private-shadow",
  "-Xlint:stars-align",
  "-Xlint:type-parameter-shadow",
  "-Ywarn-dead-code",
  "-Ywarn-extra-implicit",
  "-Ywarn-numeric-widen",
  "-Ywarn-value-discard",
  "-Ycache-plugin-class-loader:last-modified",
  "-Ycache-macro-class-loader:last-modified",
  // in case of dependency conflicts this is useful
//  "-Ylog-classpath",
  "-Ybackend-parallelism",
  "16"
)

Compile / packageDoc / publishArtifact := false

Universal / topLevelDirectory := None

Universal / javaOptions ++= Seq(
  "-J-Xmx1G",
  "-J-XX:+PrintGC"
)

Global / onChangedBuildSource := ReloadOnSourceChanges

lazy val shared = crossProject(JSPlatform, JVMPlatform)
  .in(file("shared"))
  .settings(
    name := "foo",
    version := "0.1-SNAPSHOT",
    libraryDependencies ++= Seq(
      "org.scalatest" %%% "scalatest"     % "3.1.1" % Test,
      "io.circe"      %%% "circe-parser"  % "0.14.1",
      "io.circe"      %%% "circe-generic" % "0.14.1",
      "io.circe"      %% "circe-jawn"     % "0.14.1" % "compile",
      "io.circe"      %%% "circe-literal" % "0.14.1",
      "io.circe"      %%% "circe-core"    % "0.14.1",
      "org.typelevel" %%% "cats-effect"   % "3.1.1",
      "org.typelevel" %%% "kittens"       % "2.3.2"
    )
  )
  .jvmSettings(
    libraryDependencies ++= Seq(
      "org.http4s" %% "http4s-async-http-client" % Http4sVersion,
      "org.http4s" %% "http4s-dsl"               % Http4sVersion,
      "org.http4s" %% "http4s-circe"             % Http4sVersion
    )
  )
  .jsSettings(
    libraryDependencies += "io.github.cquiroz" %%% "scala-java-time" % "2.2.2"
  )

lazy val frontend = project
  .enablePlugins(ScalaJSPlugin)
  .dependsOn(shared.js)
  .settings(
    // as it cannot find some of the sources
    Compile / fastOptJS / scalaJSLinkerConfig := (Compile / fastOptJS / scalaJSLinkerConfig).value
      .withSourceMap(false),
    jsEnv := new org.scalajs.jsenv.nodejs.NodeJSEnv(),
    Compile / npmDependencies ++= Seq(
      "react"       -> "16.13.1",
      "react-dom"   -> "16.13.1",
      "rison"       -> "0.1.1",
      "react-proxy" -> "1.1.8"
    ),
    Compile / npmDevDependencies ++= Seq(
      "file-loader"         -> "6.0.0",
      "style-loader"        -> "1.2.1",
      "css-loader"          -> "3.5.3",
      "html-webpack-plugin" -> "4.3.0",
      "copy-webpack-plugin" -> "5.1.1",
      "webpack-merge"       -> "4.2.2"
    ),
    libraryDependencies ++= Seq(
      "co.fs2"        %%% "fs2-core"   % "3.0.4",
      "me.shadaj"     %%% "slinky-web" % "0.6.5",
      "me.shadaj"     %%% "slinky-hot" % "0.6.5",
      "org.scalatest" %%% "scalatest"  % "3.1.1" % Test
    ),
    scalacOptions += "-Ymacro-annotations",
    webpack / version := "4.43.0",
    startWebpackDevServer / version := "3.11.0",
    scalaJSUseMainModuleInitializer := true
  )
  .enablePlugins(ScalaJSBundlerPlugin)

lazy val server = project
  .in(file("."))
  .dependsOn(shared.jvm)
  .dependsOn(testassist)
  .settings(
    libraryDependencies += "org.jsoup" % "jsoup" % "1.14.3"
  )

ThisBuild / scalafmtVersion := "2.0.1"

addCommandAlias(
  "fmt",
  ";scalafmt;test:scalafmt;sbt:scalafmt;frontend/scalafmt;testassist/scalafmt;testassist/test:scalafmt"
)

Global / cancelable := true

lazy val Http4sVersionNewer = "0.23.1"
lazy val testassist = project
  .settings(
    libraryDependencies ++= Seq(
      "com.google.code.findbugs" % "jsr305"                    % "3.0.2" % Compile, // Required for compiling with the mongo client
      "com.couchbase.client"     % "java-client"               % "2.7.16",
      "org.json"                 % "json"                      % "20210307",
      "co.fs2"                   %% "fs2-core"                 % "3.0.4",
      "org.scalatest"            %% "scalatest"                % "3.2.7" % Test,
      "org.http4s"               %% "http4s-async-http-client" % Http4sVersionNewer,
      "org.http4s"               %% "http4s-dsl"               % Http4sVersionNewer,
      "org.http4s"               %% "http4s-circe"             % Http4sVersionNewer,
      "io.circe"                 %% "circe-generic"            % "0.14.1",
      "io.circe"                 %% "circe-literal"            % "0.14.1",
      "com.typesafe"             % "config"                    % "1.4.1",
      "com.github.pureconfig"    %% "pureconfig"               % "0.16.0",
      "com.eed3si9n.expecty"     %% "expecty"                  % "0.15.4" % Test,
      "org.jsoup"                % "jsoup"                     % "1.14.3"
    )
  )
  .dependsOn(shared.jvm)
