package com.chmist.mill.avrolib

import coursier.{Cache, MavenRepository}
import mill._
import mill.api.Loose
import mill.define.Sources
import mill.eval.PathRef
import mill.scalalib.Lib.resolveDependencies
import mill.scalalib.{ScalaModule, _}
import os.Path

trait AvroModule extends ScalaModule {

  override def generatedSources = T { super.generatedSources() :+ compileAvro() }

  private def avroHuggerVersion: T[String] = "1.0.0-RC15"

  def avroSources: Sources = T.sources {
    millSourcePath / 'avro
  }

  def avroOptions: T[String] = T {
      Seq.empty.mkString(",")
  }

  def avroClasspath: T[Loose.Agg[PathRef]] = T {
    resolveDependencies(
      Seq(
        Cache.ivy2Local,
        MavenRepository("https://repo1.maven.org/maven2")
      ),
      Lib.depToDependency(_, "2.12.4"),
      Seq(
        ivy"com.julianpeeters::avrohugger-core:${avroHuggerVersion()}",
        ivy"com.julianpeeters::avrohugger-filesorter:${avroHuggerVersion()}",
        ivy"com.julianpeeters::avrohugger-tools:${avroHuggerVersion()}"
      )
    )
  }
  
  def compileAvro: T[PathRef] = T.persistent {
    AvroWorkerApi.avroWorker
      .compile(
        avroClasspath().map(_.path),
        avroSources().map(_.path),
        avroOptions(),
        T.ctx().dest)
  }
}
