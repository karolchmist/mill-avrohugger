import mill._
import mill.scalalib._
import $exec.plugins
import com.chmist.mill.avrohugger._

def verify() = T.command {
  val expectedSources = Set("com/example/domain/Person.scala", "com/example/domain/Team.scala")

  val result = hello.compileAvro()
  val files = os.walk(result.path)
  val generatedSources = files.filter(_.ext == "scala").map(_.relativeTo(result.path).toString())

  assert(expectedSources == generatedSources.toSet)
}

object hello extends ScalaModule with AvroHuggerModule {
  override def scalaVersion: T[String] = "2.12.7"
}
