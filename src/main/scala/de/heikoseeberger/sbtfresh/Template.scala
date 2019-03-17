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
    """|sbt.version = 1.2.8
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
           |    // scalaVersion := "2.12.8",""".stripMargin
      else
        """scalaVersion := "2.12.8","""

    s"""|
        |cancelable in Global := true
        |addCompilerPlugin("org.spire-math" %% "kind-projector" % "0.9.9")
        |addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.1" cross CrossVersion.full)
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
        |        library.scalaCheck % Test,
        |        library.scalaTest  % Test,
        |      ) ++ library.cats ++ library.akka
        |    )
        |
        |// *****************************************************************************
        |// Library dependencies
        |// *****************************************************************************
        |
        |lazy val library =
        |  new {
        |    object Version {
        |      val scalaCheck = "1.14.0"
        |      val scalaTest  = "3.0.6"
        |      val cats = "1.6.0"
        |      val `cats-effect` = "1.2.0"
        |      val `cats-mtl` = "0.5.0"
        |      val akka = "2.5.21"
        |      val akkaHttp = "10.1.7"
        |      val json4s = "3.6.5"
        |    }
        |    val scalaCheck = "org.scalacheck" %% "scalacheck" % Version.scalaCheck
        |    val scalaTest  = "org.scalatest"  %% "scalatest"  % Version.scalaTest
        |    val shapeless = "com.chuusai" %% "shapeless" % "2.3.3"
        |    val cats = Seq(
        |       "org.typelevel" %% "cats-core" % Version.cats,
        |       "org.typelevel" %% "cats-laws" % Version.cats,
        |       "org.typelevel" %% "cats-free" % Version.cats,
        |       "org.typelevel" %% "cats-effect" % Version.`cats-effect`,
        |       "org.typelevel" %% "cats-mtl-core" % Version.`cats-mtl`,
        |    )
        |    val akka = Seq(
        |      "com.typesafe.akka" %% "akka-actor" % Version.akka,
        |      "com.typesafe.akka" %% "akka-stream" % Version.akka,
        |      "com.typesafe.akka" %% "akka-actor-typed" % Version.akka,
        |      "com.typesafe.akka" %% "akka-persistence" % Version.akka,
        |      "com.typesafe.akka" %% "akka-persistence-query" % Version.akka,
        |      "com.typesafe.akka" %% "akka-stream" % Version.akka,
        |      "com.typesafe.akka" %% "akka-protobuf" % Version.akka,
        |      "com.typesafe.akka" %% "akka-slf4j" % Version.akka,
        |      "com.typesafe.akka" %% "akka-remote" % Version.akka,
        |      "com.typesafe.akka" %% "akka-testkit" % Version.akka % Test,

        |      "com.typesafe.akka" %% "akka-http-core" % Version.akkaHttp,
        |      "com.typesafe.akka" %% "akka-http" % Version.akkaHttp,
        |      "de.heikoseeberger" %% "akka-http-json4s" % "1.25.2",
        |      "org.json4s" %% "json4s-jackson" % Version.json4s,
        |      "com.typesafe.akka" %% "akka-http-testkit" % Version.akkaHttp % Test,
        |    )
        |  }
        |
        |// *****************************************************************************
        |// Settings
        |// *****************************************************************************
        |
        |lazy val settings =
        |  commonSettings ++
        |  scalafmtSettings
        |
        |lazy val commonSettings =
        |  Seq(
        |    $scalaVersion
        |    organization := "$organization",
        |    organizationName := "$author",
        |    startYear := Some($year),$licenseSettings
        |    scalacOptions ++= Seq(
        |       //A -X option suggests permanence, while a -Y could disappear at any time
        |       "-encoding", "UTF-8", // source files are in UTF-8
        |       "-explaintypes", // Explain type errors in more detail.
        |       "-deprecation", // warn about use of deprecated APIs
        |       "-unchecked", // warn about unchecked type parameters
        |       "-feature", // warn about misused language features
        |       "-language:postfixOps", // allow higher kinded types without `import scala.language.postfixOps`
        |       "-language:higherKinds", // allow higher kinded types without `import scala.language.higherKinds`
        |       "-language:implicitConversions", // Allow definition of implicit functions called views
        |       "-language:existentials", // Existential types (besides wildcard types) can be written and inferred
        |       "-language:reflectiveCalls",
        |       "-target:jvm-1.8",
        |       "-Xlint", // enable handy linter warnings
        |       //"-Xfatal-warnings", // turn compiler warnings into errors
        |       "-Ypartial-unification", // allow the compiler to unify type constructors of different arities
        |       "-Ywarn-unused-import",
        |       "-Yrangepos" //Use range positions for syntax trees.
        |    ),
        |    Compile / unmanagedSourceDirectories := Seq((Compile / scalaSource).value),
        |    Test / unmanagedSourceDirectories := Seq((Test / scalaSource).value)$wartremoverSettings,
        |)
        |
        |lazy val scalafmtSettings =
        |  Seq(
        |    scalafmtOnCompile := true,
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
           |addSbtPlugin("com.dwijnand"      % "sbt-travisci"    % "1.1.3")""".stripMargin
      else
        ""
    val wartRemoverPlugin =
      if (setUpWartremover)
        """|
           |addSbtPlugin("org.wartremover"   % "sbt-wartremover" % "2.3.1")""".stripMargin
      else
        ""

    s"""|addSbtPlugin("com.dwijnand"      % "sbt-dynver"      % "3.3.0")${travisPlugin}
        |addSbtPlugin("com.geirsson"      % "sbt-scalafmt"    % "1.5.1")
        |addSbtPlugin("de.heikoseeberger" % "sbt-header"      % "5.1.0")${wartRemoverPlugin}
        |addSbtPlugin("net.virtual-void" % "sbt-dependency-graph" % "0.9.2")
        |addSbtPlugin("ch.epfl.scala" % "sbt-scalafix" % "0.9.4")
        |addSbtPlugin("com.timushev.sbt" % "sbt-updates" % "0.4.0")
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
    """|style = defaultWithAlign
       |
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
       |  - 2.12.8
       |
       |jdk:
       |  - oraclejdk8
       |""".stripMargin
}
