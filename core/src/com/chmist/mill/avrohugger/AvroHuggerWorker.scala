package com.chmist.mill.avrohugger

import java.io.File

import mill._
import mill.api.{Ctx, PathRef}
import mill.modules.Jvm


class AvroWorker {
  private var avroInstanceCache = Option.empty[(Long, AvroWorkerApi)]

  private def avro(avroClasspath: Agg[os.Path])(implicit ctx: Ctx) = {
    val classloaderSig = avroClasspath.map(p => p.toString().hashCode + os.mtime(p)).sum
    avroInstanceCache match {
      case Some((sig, instance)) if sig == classloaderSig => instance
      case _ =>
        val instance = new AvroWorkerApi {
          override def compileAvro(sources: Seq[File], generatedDirectory: File): Unit = {
            val args: Seq[String] = Seq("generate", "schema") ++ sources.map(_.getCanonicalPath) ++ Seq(generatedDirectory.getCanonicalPath)
            Jvm.runSubprocess("avrohugger.tool.Main", avroClasspath, mainArgs = args)
          }
        }
        avroInstanceCache = Some((classloaderSig, instance))
        instance
    }
  }


  def compile(avroClasspath: Agg[os.Path], avroSources: Seq[os.Path], dest: os.Path)
             (implicit ctx: mill.api.Ctx): mill.api.Result[PathRef] = {
    val compiler = avro(avroClasspath)

    def compileAvroDir(inputDir: os.Path) {
      if (inputDir.toIO.exists) {
        val sources = os.walk(inputDir).filter(_.last.matches(".*\\.avsc"))
        compiler.compileAvro(sources.map(_.toIO), dest.toIO)
      }
    }
    avroSources.foreach(compileAvroDir)
    mill.api.Result.Success(PathRef(dest))
  }
}

trait AvroWorkerApi {
  def compileAvro(sources: Seq[File], generatedDirectory: File)
}

object AvroWorkerApi {
  def avroWorker = new AvroWorker()
}
