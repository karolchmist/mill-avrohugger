package com.chmist.mill.avrolib

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
          override def compileAvro(source: File, avroOptions: String, generatedDirectory: File): Unit = {
            Jvm.runSubprocess("avrohugger.tool.Main", avroClasspath, mainArgs = Seq("generate", "schema", source.getCanonicalPath, generatedDirectory.getCanonicalPath))
          }
        }
        avroInstanceCache = Some((classloaderSig, instance))
        instance
    }
  }


  def compile(avroClasspath: Agg[os.Path], avroSources: Seq[os.Path], avroOptions: String, dest: os.Path)
             (implicit ctx: mill.api.Ctx): mill.api.Result[PathRef] = {
    val compiler = avro(avroClasspath)

    def compileAvroDir(inputDir: os.Path) {
      // ls throws if the path doesn't exist
      println(inputDir)
      if (inputDir.toIO.exists) {
        os.walk(inputDir).filter(_.last.matches(".*.avsc"))
          .foreach { proto =>
          println(dest.toIO)
            compiler.compileAvro(proto.toIO, avroOptions, dest.toIO)
          }
      }
    }
    avroSources.foreach(compileAvroDir)
    mill.api.Result.Success(PathRef(dest))
  }
}

trait AvroWorkerApi {
  def compileAvro(source: File, avroOptions: String, generatedDirectory: File)
}

object AvroWorkerApi {
  def avroWorker = new AvroWorker()
}
