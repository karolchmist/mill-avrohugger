import mill.scalalib._
import mill.scalalib.publish._

import $ivy.`de.tototec::de.tobiasroeser.mill.integrationtest:0.1.0`, de.tobiasroeser.mill.integrationtest._

/** Run tests. */
def test() = T.command {
  integrationTest.test()()
}

/** Test and release to Maven Central. */
def release(
  sonatypeCreds: String,
  release: Boolean = true
) = T.command {
    test()()
    core.publish(sonatypeCreds = sonatypeCreds, release = release)()
}

trait AvroHuggerModule extends ScalaModule with PublishModule {
  def scalaVersion = T { "2.12.8" }

  def publishVersion = "0.0.1-SNAPSHOT"

  def ivyDeps = T {
    Agg(ivy"org.scala-lang:scala-library:${scalaVersion()}")
  }

  def millVersion = "0.3.6"

  object Deps {
    val millMain = ivy"com.lihaoyi::mill-main:${millVersion}"
    val millScalalib = ivy"com.lihaoyi::mill-scalalib:${millVersion}"
  }

  def javacOptions = Seq("-source", "1.8", "-target", "1.8")

  def pomSettings = T {
    PomSettings(
      description = "Mill module adding avrohugger support",
      organization = "com.chmist",
      url = "https://github.com/karolchmist/mill-avrohugger",
      licenses = Seq(License.`Apache-2.0`),
      versionControl = VersionControl.github("karolchmist", "mill-avrohugger"),
      developers = Seq(Developer("karolchmist", "Karol Chmist", "https.//github.com/karolchmist"))
    )
  }

}

object core extends AvroHuggerModule {
  override def artifactName = T { "com.chmist.mill.avrohugger" }

  def compileIvyDeps = Agg(Deps.millMain, Deps.millScalalib)
}

object integrationTest extends MillIntegrationTestModule {
  def millTestVersion = T {
    val ctx = T.ctx()
    ctx.env.get("TEST_MILL_VERSION").getOrElse(core.millVersion)
  }

  def pluginsUnderTest = Seq(core)
}
