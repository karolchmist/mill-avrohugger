import mill._
import mill.scalalib._
import $exec.plugins
import com.chmist.mill.avrohugger._

def verify() = T.command {
    val expectedClasses = Set(
      "com/example/HelloTeam.class",
      "com/example/domain/Person.class",
      "com/example/domain/Team.class",
      "com/example/domain/Person$.class",
      "com/example/domain/Team$.class"
    )

    val result = hello.compile()
    val classFiles = os
      .walk(result.classes.path)
      .filter(_.ext == "class")
      .map(_.relativeTo(result.classes.path).toString())

    assert(classFiles.toSet == expectedClasses)
  }

object hello extends ScalaModule with AvroHuggerModule {
    override def scalaVersion: T[String] = "2.12.7"
  }
