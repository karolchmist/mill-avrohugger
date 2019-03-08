import mill._
import mill.scalalib._
import $exec.plugins
import com.chmist.mill.avrolib._
import os.Path

def verify() = T.command {
  val expectedSources = Seq("com/example/tutorial/Person.scala")
  val result:mill.eval.PathRef = hello.compileAvro()
  val files: IndexedSeq[Path] = os.walk(result.path)
  val generatedSources = files.map(_.relativeTo(result.path)).filter(_.ext == "scala").map(_.toString())
  assert(expectedSources.toSet == generatedSources.toSet)
}

object hello extends ScalaModule with AvroModule {
  override def scalaVersion: T[String] = "2.12.7"
}
