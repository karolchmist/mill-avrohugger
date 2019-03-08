package com.chmist.mill.avrohugger

import coursier.{Cache, MavenRepository}
import mill._
import mill.api.Loose
import mill.define.Sources
import mill.eval.PathRef
import mill.scalalib.Lib.resolveDependencies
import mill.scalalib.{ScalaModule, _}

trait AvroHuggerModule extends ScalaModule {

  override def generatedSources = T { super.generatedSources() :+ compileAvro() }

  def avroHuggerVersion: T[String] = "1.0.0-RC15"

  def avroSources: Sources = T.sources {
    millSourcePath / 'avro
  }

  def avroClasspath: T[Loose.Agg[PathRef]] = T {
    resolveDependencies(
      Seq(
        Cache.ivy2Local,
        MavenRepository("https://repo1.maven.org/maven2")
      ),
      Lib.depToDependency(_, "2.12.7"),
      Seq(
        ivy"com.julianpeeters::avrohugger-tools:${avroHuggerVersion()}"
      )
    )
  }
  
  def compileAvro: T[PathRef] = T.persistent {
    AvroWorkerApi.avroWorker
      .compile(
        avroClasspath().map(_.path),
        avroSources().map(_.path),
        T.ctx().dest)
  }
}
