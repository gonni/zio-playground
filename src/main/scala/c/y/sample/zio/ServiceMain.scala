package c.y.sample.zio


import zio._

import java.io.IOException
import scala.collection.mutable

case class Doc(
                title: String,
                description: String,
                language: String,
                format: String,
                content: Array[Byte]
              )

case class Metadata(
                     title: String,
                     description: String,
                     language: String,
                     format: String
                   )

trait MetadataRepo {
  def get(id: String): ZIO[Any, Throwable, Metadata]
  def put(id: String, metadata: Metadata): ZIO[Any, Throwable, Unit]
  def delete(id: String): ZIO[Any, Throwable, Unit]
  def findByTitle(title: String): ZIO[Any, Throwable, Map[String, Metadata]]
}

trait BlobStorage {
  def get(id: String): ZIO[Any, Throwable, Array[Byte]]
  def put(content: Array[Byte]): ZIO[Any, Throwable, String]
  def delete(id: String): ZIO[Any, Throwable, Unit]
}

trait DocRepo {
  def get(id: String): ZIO[Any, Throwable, Doc]
  def save(document: Doc): ZIO[Any, Throwable, String]
  def delete(id: String): ZIO[Any, Throwable, Unit]
  def findByTitle(title: String): ZIO[Any, Throwable, List[Doc]]
}

case class DocRepoImpl(
                        metadataRepo: MetadataRepo,
                        blobStorage: BlobStorage
                      ) extends DocRepo {
  override def get(id: String): ZIO[Any, Throwable, Doc] =
    for {
      metadata <- metadataRepo.get(id)
      content <- blobStorage.get(id)
    } yield Doc(
      metadata.title,
      metadata.description,
      metadata.language,
      metadata.format,
      content
    )

  override def save(document: Doc): ZIO[Any, Throwable, String] =
    for {
      id <- blobStorage.put(document.content)
      _ <- metadataRepo.put(
        id,
        Metadata(
          document.title,
          document.description,
          document.language,
          document.format
        )
      )
    } yield id

  override def delete(id: String): ZIO[Any, Throwable, Unit] =
    for {
      _ <- blobStorage.delete(id)
      _ <- metadataRepo.delete(id)
    } yield ()

  override def findByTitle(title: String): ZIO[Any, Throwable, List[Doc]] =
    for {
      map <- metadataRepo.findByTitle(title)
      content <- ZIO.foreach(map)((id, metadata) =>
        for {
          content <- blobStorage.get(id)
        } yield id -> Doc(
          metadata.title,
          metadata.description,
          metadata.language,
          metadata.format,
          content
        )
      )
    } yield content.values.toList
}

object DocRepoImpl {
  val layer: ZLayer[BlobStorage with MetadataRepo, Nothing, DocRepo] =
    ZLayer {
      for {
        metadataRepo <- ZIO.service[MetadataRepo]
        blobStorage  <- ZIO.service[BlobStorage]
      } yield DocRepoImpl(metadataRepo, blobStorage)
    }
}

object DocRepo {
  def get(id: String): ZIO[DocRepo, Throwable, Doc] =
    ZIO.serviceWithZIO[DocRepo](_.get(id))

  def save(document: Doc): ZIO[DocRepo, Throwable, String] =
    ZIO.serviceWithZIO[DocRepo](_.save(document))

  def delete(id: String): ZIO[DocRepo, Throwable, Unit] =
    ZIO.serviceWithZIO[DocRepo](_.delete(id))

  def findByTitle(title: String): ZIO[DocRepo, Throwable, List[Doc]] =
    ZIO.serviceWithZIO[DocRepo](_.findByTitle(title))
}

object InmemoryBlobStorage {
  class BlobStorageImpl extends BlobStorage {
    val data = mutable.Map[String, Array[Byte]]()

    override def get(id: String): ZIO[Any, Throwable, Array[Byte]] = ZIO.succeed(Array(1,2,3))

    override def put(content: Array[Byte]): ZIO[Any, Throwable, String] = ZIO.succeed{
      new String(content, "UTF-8")
    }

    override def delete(id: String): ZIO[Any, Throwable, Unit] =
      Console.printLine(s"[BLOBStg]delete id#$id")
  }

  val layer : ZLayer[Any, Nothing, BlobStorage] =
    ZLayer.succeed(new BlobStorageImpl)
}

object InmemoryMetadataRepo {

  class MetadataRepoImpl extends MetadataRepo {
    override def get(id: String): ZIO[Any, Throwable, Metadata] =
      ZIO.succeed(Metadata(id, "a", "b", "c"))
    override def put(id: String, metadata: Metadata): ZIO[Any, Throwable, Unit] =
      Console.printLine(s"Put Data #$id")
    override def delete(id: String): ZIO[Any, Throwable, Unit] =
      Console.printLine(s"[MetadataStg]Delete Data #$id")
    override def findByTitle(title: String): ZIO[Any, Throwable, Map[String, Metadata]] =
      ZIO.succeed(Map("id1" -> Metadata("id1", title, "b", "c")))
  }

  val layer: ZLayer[Any, Nothing, MetadataRepo] =
    ZLayer.succeed(new MetadataRepoImpl)
}

object ServiceMain extends ZIOAppDefault {
  val app =
    for {
      id <-
        DocRepo.save(
          Doc(
            "title",
            "description",
            "en",
            "text/plain",
            "content".getBytes()
          )
        )
      doc <- DocRepo.get(id)
      _ <- Console.printLine(
        s"""
           |[LOG]
           |Downloaded the document with $id id:
           |  title: ${doc.title}
           |  description: ${doc.description}
           |  language: ${doc.language}
           |  format: ${doc.format}
           |""".stripMargin
      )
      _ <- DocRepo.delete(id)
      _ <- Console.printLine(s"[LOG] Deleted the document with $id id")
    } yield ()

  def run =
    app.provide(
      DocRepoImpl.layer,
      InmemoryBlobStorage.layer,
      InmemoryMetadataRepo.layer
    )
}