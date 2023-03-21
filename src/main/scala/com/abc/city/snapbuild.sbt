addCommandAlias(
  name = "integration-fast",
  value = "crackle / IntegrationTest / testOnly -- -l MyaIntegrationTest  -l QuestIntegrationTest"
)
addCommandAlias(
  name = "integration-quest",
  value = "crackle / IntegrationTest / testOnly -- -n QuestIntegrationTest"
)
addCommandAlias(
  name = "integration-mya",
  value = "crackle / IntegrationTest / testOnly -- -n MyaIntegrationTest"
)

addCommandAlias("fmt", ";scalafmt;test:scalafmt;it:scalafmt;sbt:scalafmt")
addCommandAlias("check-fmt", ";scalafmt::test;test:scalafmt::test;it:scalafmt::test;sbt:scalafmt::test")
addCommandAlias("ci", ";check-fmt;test;integration-fast")

addCommandAlias("examples", "testOnly crackle.PriceRequestJsonExamplesTest")

ThisBuild / organization := "snap"
ThisBuild / scalaVersion := "2.13.8"
ThisBuild / version := sys.props.get("build.number").getOrElse("DEV-BUILD")

val Http4sVersion    = "0.21.31"
val TsecVersion      = "0.2.1"
val LogbackVersion   = "1.2.10"
val ScalatestVersion = "3.2.10"
val CirceVersion     = "0.14.1"
val MonocleVersion   = "2.1.0"
val LuceneVersion    = "8.9.0"
val NettyVersion     = "4.1.74.Final"

ThisBuild / scalacOptions ++= Seq(
  "-Ymacro-annotations",
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
  "-Xlint:infer-any",
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
  "-Ywarn-unused:implicits",
  "-Ywarn-unused:imports",
  "-Ywarn-unused:locals",
  "-Ywarn-unused:patvars",
  "-Ywarn-unused:privates",
  "-Ywarn-value-discard",
  "-Ycache-plugin-class-loader:last-modified",
  "-Ycache-macro-class-loader:last-modified",
  "-Ybackend-parallelism",
  "16"
)

lazy val TestAndIntegrationTest = ConfigRef("test,it")

lazy val root = project
  .in(file("."))
  .settings(
    name := "snap"
  )
  .aggregate(crackle)

lazy val crackle = project
  .enablePlugins(JavaAppPackaging)
  .configs(IntegrationTest.extend(Test))
  .settings(Defaults.itSettings)
  .settings(
    Compile / mainClass := Some("crackle.Server"),
    Test / reStart / mainClass := Some("crackle.LocalDevServer"),
    reStart / javaOptions += "-Dlogback.debug=false",
    // Uncomment me to remote debug the local dev server
    // reStart / javaOptions ++= Seq("-Xdebug", "-Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=5005")
    Compile / packageDoc / publishArtifact := false,
    ideExcludedDirectories := Seq(file("assets")),
    autoAPIMappings := true,
    inConfig(Test) { // Make restart work in the test scope, where the test dev server lives
      import spray.revolver.Actions._

      reStart := Def
        .inputTask {
          restartApp(
            streams.value,
            reLogTag.value,
            thisProjectRef.value,
            reForkOptions.value,
            (reStart / mainClass).value,
            (reStart / fullClasspath).value,
            reStartArgs.value,
            startArgsParser.parsed
          )
        }
        .dependsOn(Compile / products)
        .dependsOn((Test / testQuick).toTask(""))
        .dependsOn(IntegrationTest / compile)
        .evaluated
    }
  )
  .settings(crackleDeps)

lazy val fakeQuest = project
  .in(file("fakequest"))
  .enablePlugins(JavaAppPackaging)
  .settings(
    name := "FakeQuest",
    version := (crackle / version).value,
    Compile / mainClass := Some("crackle.quest.FakeQuest"),
    scriptClasspath += (crackle / Test / packageBin).value.getName,
    Compile / packageDoc / publishArtifact := false,
    Universal / mappings += {
      val testjar = (crackle / Test / packageBin).value
      testjar -> s"lib/${testjar.getName}"
    },
    ideSkipProject := true
  )
  .dependsOn(crackle % "compile->test")

lazy val crackleDeps = Seq(
  libraryDependencies ++= Seq(
    "org.scala-lang" % "scala-reflect"        % scalaVersion.value,
    "com.nrinaudo"   %% "kantan.csv"          % "0.6.1",
    "org.http4s"     %% "http4s-blaze-server" % Http4sVersion,
    "org.http4s"     %% "http4s-dsl"          % Http4sVersion,
    "ch.qos.logback" % "logback-classic"      % LogbackVersion,
    "org.bouncycastle" % "bcprov-jdk15on" % "1.69",
    "net.logstash.logback"       % "logstash-logback-encoder"      % "5.2" exclude ("com.fasterxml.jackson.core", "jackson-databind"),
    "javax.xml.bind"             % "jaxb-api"                      % "2.3.1",
    "com.fasterxml.jackson.core" % "jackson-databind"              % "2.13.2.2",
    "org.http4s"                 %% "http4s-async-http-client"     % Http4sVersion,
    "org.http4s"                 %% "http4s-circe"                 % Http4sVersion,
    "org.http4s"                 %% "http4s-prometheus-metrics"    % Http4sVersion,
    "io.github.jmcardon"         %% "tsec-http4s"                  % TsecVersion,
    "io.github.jmcardon"         %% "tsec-jwt-sig"                 % TsecVersion,
    "io.circe"                   %% "circe-generic"                % CirceVersion,
    "io.circe"                   %% "circe-generic-extras"         % CirceVersion,
    "io.circe"                   %% "circe-derivation"             % "0.13.0-M5",
    "io.circe"                   %% "circe-literal"                % CirceVersion,
    "io.circe"                   %% "circe-parser"                 % CirceVersion,
    "io.circe"                   %% "circe-optics"                 % "0.14.1",
    "com.github.julien-truffaut" %% "monocle-core"                 % MonocleVersion,
    "com.github.julien-truffaut" %% "monocle-macro"                % MonocleVersion,
    "org.apache.lucene"          % "lucene-core"                   % LuceneVersion,
    "org.apache.lucene"          % "lucene-queryparser"            % LuceneVersion,
    "org.apache.lucene"          % "lucene-analyzers-common"       % LuceneVersion,
    "org.apache.lucene"          % "lucene-suggest"                % LuceneVersion,
    "org.mongodb.scala"          %% "mongo-scala-driver"           % "4.3.0",
    "org.mongodb"                % "mongodb-driver-core"           % "4.3.0",
    "org.mongodb"                % "mongodb-driver-sync"           % "4.3.0",
    "io.netty"                   % "netty-all"                     % NettyVersion,
    "io.netty"                   % "netty-codec"                   % NettyVersion,
    "io.netty"                   % "netty-codec-http"              % NettyVersion,
    "io.netty"                   % "netty-codec-socks"             % NettyVersion,
    "io.netty"                   % "netty-handler"                 % NettyVersion,
    "io.netty"                   % "netty-handler-proxy"           % NettyVersion,
    "io.netty"                   % "netty-transport"               % NettyVersion,
    "io.netty"                   % "netty-transport-native-epoll"  % NettyVersion,
    "io.netty"                   % "netty-transport-native-kqueue" % NettyVersion,
    "io.netty"                   % "netty-resolver"                % NettyVersion,
    "io.netty"                   % "netty-buffer"                  % NettyVersion,
    "io.netty"                   % "netty-common"                  % NettyVersion,
    "com.beachape"               %% "enumeratum"                   % "1.7.0",
    "org.typelevel"              %% "kittens"                      % "2.3.2",
    "com.github.fd4s"            %% "fs2-kafka"                    % "1.7.0",
    "com.google.code.findbugs"   % "jsr305"                        % "3.0.2" % Compile, // Required for compiling with the mongo client
    "org.scalatest"              %% "scalatest"                    % ScalatestVersion % TestAndIntegrationTest,
    "org.scalacheck"             %% "scalacheck"                   % "1.15.4" % TestAndIntegrationTest,
    "com.ironcorelabs"           %% "cats-scalatest"               % "3.1.1" % Test,
    "org.scalikejdbc"            %% "scalikejdbc"                  % "3.5.0" % TestAndIntegrationTest,
    "com.microsoft.sqlserver"    % "mssql-jdbc"                    % "6.4.0.jre8" % TestAndIntegrationTest,
    "org.bitbucket.cowwoc"       % "diff-match-patch"              % "1.2" % Test,
    "com.eed3si9n.expecty"       %% "expecty"                      % "0.15.4" % TestAndIntegrationTest,
    "io.github.embeddedkafka"    %% "embedded-kafka"               % "2.8.1" % Test exclude ("log4j", "log4j")
      exclude ("io.netty", "netty-handler")
      exclude ("io.netty", "netty-transport-native-epoll"),
    /** Exclusion and explicit dependency are there in order to push commons-beanutils to the latest version */
    "org.jxls"             % "jxls-poi"          % "2.10.0" % Test exclude ("commons-beanutils", "commons-beanutils"),
    "commons-beanutils"    % "commons-beanutils" % "1.9.4"  % Test,
    "com.stephenn"         %% "scalatest-circe"  % "0.2.5"  % Test,
    "org.apache.zookeeper" % "zookeeper"         % "3.7.0"  % Test exclude ("log4j", "log4j")
      exclude ("io.netty", "netty-handler")
      exclude ("io.netty", "netty-transport-native-epoll"),
    "org.apache.kafka"   % "kafka-clients"    % "2.8.1",
    "commons-codec"      % "commons-codec"    % "1.15",
    "org.apache.commons" % "commons-compress" % "1.21"
  ),
  dependencyOverrides ++= Seq(
    "org.scala-lang"             % "scala-library"                 % scalaVersion.value,
    "io.netty"                   % "netty-codec-http"              % NettyVersion,
    "io.netty"                   % "netty-codec-socks"             % NettyVersion,
    "io.netty"                   % "netty-handler"                 % NettyVersion,
    "io.netty"                   % "netty-handler-proxy"           % NettyVersion,
    "io.netty"                   % "netty-transport-native-epoll"  % NettyVersion,
    "io.netty"                   % "netty-transport-native-kqueue" % NettyVersion,
    "io.netty"                   % "netty-buffer"                  % NettyVersion
  ),
  addCompilerPlugin("com.olegpy"    %% "better-monadic-for" % "0.3.1"),
  addCompilerPlugin("org.typelevel" %% "kind-projector"     % "0.10.3")
)
