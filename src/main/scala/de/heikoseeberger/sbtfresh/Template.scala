/*
 * Copyright 2016 Heiko Seeberger
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.heikoseeberger.sbtfresh

import de.heikoseeberger.sbtfresh.license.License
import java.time.LocalDate.now

private object Template {

  private val year = now().getYear

  def buildProperties: String =
    """|sbt.version = 1.3.8
       |""".stripMargin

  def buildJvmOptions: String =
    """|-Xss8m
       |-Xms1G
       |-Xmx1G""".stripMargin

  def buildSbt(organization: String,
               name: String,
               packageSegments: Vector[String],
               author: String,
               license: Option[License],
               setUpTravis: Boolean,
               setUpWartremover: Boolean): String = {
    val nameIdentifier = if (name.segments.mkString == name) name else s"`$name`"

    val licenseSettings = {
      def settings(license: License) = {
        val License(_, name, url) = license
        s"""|
            |    licenses += ("$name", url("$url")),""".stripMargin
      }
      license.map(settings).getOrElse("")
    }

    val wartremoverSettings =
      if (setUpWartremover)
        """|,
           |    Compile / compile / wartremoverWarnings ++= Warts.unsafe""".stripMargin
      else
        ""

    val scalaVersion =
      if (setUpTravis)
        """|// scalaVersion from .travis.yml via sbt-travisci
           |    // scalaVersion := "2.13.1",""".stripMargin
      else
        """scalaVersion := "2.13.1","""

    s"""|cancelable in Global := true
        |addCompilerPlugin("org.typelevel" %% "kind-projector" % "0.11.0" cross CrossVersion.full)
        |addCompilerPlugin(scalafixSemanticdb)
        |
        |// *****************************************************************************
        |// Projects
        |// *****************************************************************************
        |
        |lazy val $nameIdentifier =
        |  project
        |    .in(file("."))
        |    .enablePlugins(AutomateHeaderPlugin)
        |    .settings(settings)
        |    .settings(
        |      libraryDependencies ++= Seq(
        |        library.shapeless,
        |        library.simulacrum,
        |        library.scalaLogging,
        |        library.`logback-classic`,
        |        library.scalaCheck             % Test,
        |        library.scalaTest              % Test,
        |        library.`scalacheck-shapeless` % Test,
        |        library.scalamock              % Test
        |      ) ++ library.cats ++ library.akka ++ library.http4s
        |      ++ library.fs2 ++ library.circe ++ library.metrics
        |      ++ library.specs2
        |    )
        |
        |// *****************************************************************************
        |// Library dependencies
        |// *****************************************************************************
        |
        |resolvers += Resolver.sonatypeRepo("snapshots")
        |
        |lazy val library =
        |  new {
        |    object Version {
        |      val scalaCheck              = "1.14.3"
        |      val scalaTest               = "3.1.0"
        |      val cats                    = "2.1.0"
        |      val `cats-effect`           = "2.1.1"
        |      val `cats-mtl`              = "0.7.0"
        |      val akka                    = "2.6.3"
        |      val akkaHttp                = "10.1.11"
        |      val akkaKafka               = "2.0.2"
        |      val json4s                  = "3.6.7"
        |      val http4s                  = "0.21.1"
        |      val fs2                     = "2.2.2"
        |      val circe                   = "0.13.0"
        |      val codahale                = "4.1.2"
        |      val prometheus              = "0.8.1"
        |      val micrometer              = "1.3.5"
        |      val shapeless               = "2.3.3"
        |      val simulacrum              = "1.0.0"
        |      val `scalacheck-shapeless`  = "1.2.4"
        |      val `akka-http-marshallers` = "1.31.0"
        |      val specs2                  = "4.8.3"
        |    }
        |    val scalaLogging           = "com.typesafe.scala-logging" %% "scala-logging"             % "3.9.2"
        |    val `logback-classic`      = "ch.qos.logback"             % "logback-classic"            % "1.2.3"
        |    val scalaCheck             = "org.scalacheck"             %% "scalacheck"                % Version.scalaCheck
        |    val scalaTest              = "org.scalatest"              %% "scalatest"                 % Version.scalaTest
        |    val shapeless              = "com.chuusai"                %% "shapeless"                 % Version.shapeless
        |    val `scalacheck-shapeless` = "com.github.alexarchambault" %% "scalacheck-shapeless_1.14" % Version.`scalacheck-shapeless`
        |    val scalamock              = "org.scalamock"              %% "scalamock"                 % "4.4.0"
        |    val simulacrum             = "org.typelevel"              %% "simulacrum"                % Version.simulacrum
        |    val specs2 = Seq(
        |      "org.specs2" %% "specs2-core"       % Version.specs2 % Test,
        |      "org.specs2" %% "specs2-scalacheck" % Version.specs2 % Test,
        |      "org.specs2" %% "specs2-cats"       % Version.specs2 % Test,
        |      "org.specs2" %% "specs2-shapeless"  % Version.specs2 % Test
        |    )
        |    val cats = Seq(
        |      "org.typelevel" %% "cats-core"     % Version.cats,
        |      "org.typelevel" %% "cats-laws"     % Version.cats,
        |      "org.typelevel" %% "cats-free"     % Version.cats,
        |      "org.typelevel" %% "cats-effect"   % Version.`cats-effect`,
        |      "org.typelevel" %% "cats-mtl-core" % Version.`cats-mtl`
        |    )
        |    val fs2 = Seq(
        |      "co.fs2" %% "fs2-core"             % Version.fs2,
        |      "co.fs2" %% "fs2-io"               % Version.fs2,
        |      "co.fs2" %% "fs2-reactive-streams" % Version.fs2,
        |      "co.fs2" %% "fs2-experimental"     % Version.fs2
        |    )
        |    val http4s = Seq(
        |      "org.http4s" %% "http4s-dsl"            % Version.http4s,
        |      "org.http4s" %% "http4s-server"         % Version.http4s,
        |      "org.http4s" %% "http4s-json4s-jackson" % Version.http4s,
        |      "org.http4s" %% "http4s-circe"          % Version.http4s,
        |      "org.http4s" %% "http4s-blaze-server"   % Version.http4s,
        |      "org.http4s" %% "http4s-blaze-client"   % Version.http4s
        |    )
        |    val circe = Seq(
        |      "io.circe" %% "circe-core"    % Version.circe,
        |      "io.circe" %% "circe-generic" % Version.circe,
        |      "io.circe" %% "circe-parser"  % Version.circe
        |    )
        |    val akka = Seq(
        |      "com.typesafe.akka" %% "akka-actor"                  % Version.akka,
        |      "com.typesafe.akka" %% "akka-actor-testkit-typed"    % Version.akka % Test,
        |      "com.typesafe.akka" %% "akka-actor-typed"            % Version.akka,
        |      "com.typesafe.akka" %% "akka-cluster"                % Version.akka,
        |      "com.typesafe.akka" %% "akka-cluster-tools"          % Version.akka,
        |      "com.typesafe.akka" %% "akka-cluster-metrics"        % Version.akka,
        |      "com.typesafe.akka" %% "akka-cluster-sharding"       % Version.akka,
        |      "com.typesafe.akka" %% "akka-cluster-sharding-typed" % Version.akka,
        |      "com.typesafe.akka" %% "akka-cluster-tools"          % Version.akka,
        |      "com.typesafe.akka" %% "akka-cluster-typed"          % Version.akka,
        |      "com.typesafe.akka" %% "akka-distributed-data"       % Version.akka,
        |      "com.typesafe.akka" %% "akka-http"                   % Version.akkaHttp,
        |      "com.typesafe.akka" %% "akka-http-core"              % Version.akkaHttp,
        |      "com.typesafe.akka" %% "akka-http-jackson"           % Version.akkaHttp,
        |      "com.typesafe.akka" %% "akka-http-testkit"           % Version.akkaHttp,
        |      "com.typesafe.akka" %% "akka-multi-node-testkit"     % Version.akka % Test,
        |      "com.typesafe.akka" %% "akka-osgi"                   % Version.akka,
        |      "com.typesafe.akka" %% "akka-persistence"            % Version.akka,
        |      "com.typesafe.akka" %% "akka-persistence-query"      % Version.akka,
        |      "com.typesafe.akka" %% "akka-persistence-tck"        % Version.akka,
        |      "com.typesafe.akka" %% "akka-persistence-typed"      % Version.akka,
        |      "com.typesafe.akka" %% "akka-remote"                 % Version.akka,
        |      "com.typesafe.akka" %% "akka-slf4j"                  % Version.akka,
        |      "com.typesafe.akka" %% "akka-stream"                 % Version.akka,
        |      "com.typesafe.akka" %% "akka-stream-kafka"           % Version.akkaKafka,
        |      "com.typesafe.akka" %% "akka-stream-testkit"         % Version.akka % Test,
        |      "com.typesafe.akka" %% "akka-stream-typed"           % Version.akka,
        |      "com.typesafe.akka" %% "akka-testkit"                % Version.akka % Test,
        |      "de.heikoseeberger" %% "akka-http-json4s"            % Version.`akka-http-marshallers`,
        |      "de.heikoseeberger" %% "akka-http-circe"             % Version.`akka-http-marshallers`,
        |      "org.json4s"        %% "json4s-jackson"              % Version.json4s
        |    )
        |
        |    val metrics = Seq(
        |      "io.micrometer"         % "micrometer-core"                % Version.micrometer,
        |      "io.micrometer"         % "micrometer-registry-prometheus" % Version.micrometer,
        |      "io.micrometer"         % "micrometer-registry-graphite"   % Version.micrometer,
        |      "io.dropwizard.metrics" % "metrics-core"                   % Version.codahale,
        |      "io.dropwizard.metrics" % "metrics-httpclient"             % Version.codahale,
        |      "io.dropwizard.metrics" % "metrics-graphite"               % Version.codahale,
        |      "io.dropwizard.metrics" % "metrics-jvm"                    % Version.codahale,
        |      "io.dropwizard.metrics" % "metrics-json"                   % Version.codahale,
        |      "io.dropwizard.metrics" % "metrics-jmx"                    % Version.codahale,
        |      "io.dropwizard.metrics" % "metrics-healthchecks"           % Version.codahale,
        |      "io.dropwizard.metrics" % "metrics-logback"                % Version.codahale,
        |      "io.prometheus"         % "simpleclient"                   % Version.prometheus,
        |      "io.prometheus"         % "simpleclient_common"            % Version.prometheus,
        |      "io.prometheus"         % "simpleclient_hotspot"           % Version.prometheus,
        |      "io.prometheus"         % "simpleclient_dropwizard"        % Version.prometheus,
        |      "io.prometheus"         % "simpleclient_servlet"           % Version.prometheus
        |    )
        |
        |  }
        |
        |// *****************************************************************************
        |// Settings
        |// *****************************************************************************
        |
        |lazy val settings = commonSettings ++ scalafmtSettings
        |
        |lazy val commonSettings =
        |  Seq(
        |    scalaVersion := "2.13.1",
        |    organization := "default",
        |    organizationName := "mertinan",
        |    startYear := Some(2019),
        |    licenses += ("Apache-2.0", url("http://www.apache.org/licenses/LICENSE-2.0")),
        |    scalacOptions ++= Seq(
        |      //A -X option suggests permanence, while a -Y could disappear at any time
        |      "-encoding",
        |      "UTF-8", // source files are in UTF-8
        |      "-explaintypes", // Explain type errors in more detail.
        |      "-deprecation", // warn about use of deprecated APIs
        |      "-unchecked", // warn about unchecked type parameters
        |      "-feature", // warn about misused language features
        |      "-language:postfixOps", // allow higher kinded types without `import scala.language.postfixOps`
        |      "-language:higherKinds", // allow higher kinded types without `import scala.language.higherKinds`
        |      "-language:implicitConversions", // Allow definition of implicit functions called views
        |      "-language:existentials", // Existential types (besides wildcard types) can be written and inferred
        |      "-language:reflectiveCalls",
        |      "-target:jvm-1.8",
        |      "-Xlint", // enable handy linter warnings
        |      //"-Xfatal-warnings", // turn compiler warnings into errors
        |      //"-Ypartial-unification", // allow the compiler to unify type constructors of different arities
        |      //"-Ywarn-unused-import",
        |      "-Yrangepos" //Use range positions for syntax trees.
        |    ),
        |    Compile / unmanagedSourceDirectories := Seq((Compile / scalaSource).value),
        |    Test / unmanagedSourceDirectories := Seq((Test / scalaSource).value)
        |  )
        |
        |lazy val scalafmtSettings =
        |  Seq(
        |    scalafmtOnCompile := true
        |  )
        |""".stripMargin
  }

  def gitignore: String =
    """|# sbt
       |lib_managed
       |project/project
       |target
       |
       |# Worksheets (Eclipse or IntelliJ)
       |*.sc
       |
       |# Eclipse
       |.cache*
       |.classpath
       |.project
       |.scala_dependencies
       |.settings
       |.target
       |.worksheet
       |
       |# IntelliJ
       |.idea
       |
       |# ENSIME
       |.ensime
       |.ensime_lucene
       |.ensime_cache
       |
       |# Mac
       |.DS_Store
       |
       |# Akka
       |ddata*
       |journal
       |snapshots
       |
       |# Log files
       |*.log
       |
       |# jenv
       |.java-version
       |.bloop/
       |.metals/
       |""".stripMargin

  def notice(author: String): String =
    s"""|Copyright $year $author
        |""".stripMargin

  def `package`(packageSegments: Vector[String], author: String): String = {
    val superPackage = packageSegments.init.mkString(".")
    val lastSegment  = packageSegments.last
    s"""|
        |package $superPackage
        |
        |package object $lastSegment {
        |
        |  type Traversable[+A] = scala.collection.immutable.Traversable[A]
        |  type Iterable[+A]    = scala.collection.immutable.Iterable[A]
        |  type Seq[+A]         = scala.collection.immutable.Seq[A]
        |  type IndexedSeq[+A]  = scala.collection.immutable.IndexedSeq[A]
        |}
        |""".stripMargin
  }

  def plugins(setUpTravis: Boolean, setUpWartremover: Boolean): String = {
    val travisPlugin =
      if (setUpTravis)
        """|
           |addSbtPlugin("com.dwijnand"      % "sbt-travisci"    % "1.2.0")""".stripMargin
      else
        ""
    val wartRemoverPlugin =
      if (setUpWartremover)
        """|
           |addSbtPlugin("org.wartremover"   % "sbt-wartremover" % "2.4.2")""".stripMargin
      else
        ""

    s"""|addSbtPlugin("com.dwijnand"      % "sbt-dynver"           % "3.3.0")${travisPlugin}
        |addSbtPlugin("org.scalameta"     % "sbt-scalafmt"         % "2.0.1")
        |addSbtPlugin("de.heikoseeberger" % "sbt-header"           % "5.1.0")${wartRemoverPlugin}
        |addSbtPlugin("net.virtual-void"  % "sbt-dependency-graph" % "0.9.2")
        |addSbtPlugin("ch.epfl.scala"      % "sbt-scalafix"          % "0.9.7-1")
        |addSbtPlugin("com.timushev.sbt"  % "sbt-updates"          % "0.4.0")
        |addSbtPlugin("io.spray"          % "sbt-revolver"         % "0.9.1")
        |
        |""".stripMargin
  }

  def readme(name: String, license: Option[License]): String = {
    val licenseText = {
      def text(license: License) = {
        val License(_, name, url) = license
        s"""|## License ##
            |
            |This code is open source software licensed under the
            |[$name]($url) license.""".stripMargin
      }
      license.map(text).getOrElse("")
    }
    s"""|# $name #
        |
        |Welcome to $name!
        |
        |## Contribution policy ##
        |
        |Contributions via GitHub pull requests are gladly accepted from their original author. Along with
        |any pull requests, please state that the contribution is your original work and that you license
        |the work to the project under the project's open source license. Whether or not you state this
        |explicitly, by submitting any copyrighted material via pull request, email, or other means you
        |agree to license the material under the project's open source license and warrant that you have the
        |legal authority to do so.
        |
        |$licenseText
        |""".stripMargin
  }

  def scalafmtConf: String =
    """|version                           = 2.0.1
       |style                             = defaultWithAlign
       |danglingParentheses               = true
       |indentOperator                    = spray
       |maxColumn                         = 100
       |newlines.alwaysBeforeMultilineDef = true
       |project.excludeFilters            = [".*\\.sbt"]
       |rewrite.rules                     = [AsciiSortImports, RedundantBraces, RedundantParens]
       |spaces.inImportCurlyBraces        = true
       |unindentTopLevelOperators         = true
       |""".stripMargin

  def travisYml: String =
    """|language: scala
       |
       |scala:
       |  - 2.13.1
       |
       |jdk:
       |  - oraclejdk8
       |""".stripMargin
}
