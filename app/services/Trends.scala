package services

import model.{Counter, HadithResult}

import scala.collection.concurrent.TrieMap
import scala.concurrent.{ExecutionContext, Future}

trait TrendsApi {
  def recordEvent(searchQuery: String, results: Seq[HadithResult])(implicit ec: ExecutionContext)

  def chapters(limit: Int)(implicit ec: ExecutionContext): Future[Seq[Counter]]

  def books(limit: Int)(implicit ec: ExecutionContext): Future[Seq[Counter]]

  def grads(limit: Int)(implicit ec: ExecutionContext): Future[Seq[Counter]]
}

class InMemoryTrends extends TrendsApi {
  val chaptersCounter: TrieMap[String, Int] = TrieMap.empty
  val booksCounter: TrieMap[String, Int] = TrieMap.empty
  val gradsCounter: TrieMap[String, Int] = TrieMap.empty

  override def recordEvent(searchQuery: String, results: Seq[HadithResult])(implicit ec: ExecutionContext): Unit = {
    results.take(10).foreach { result =>
      chaptersCounter += result.chapter.noTashkeelContent -> 1
      booksCounter += result.book.noTashkeelContent -> 1
      //TODO: Add after adding grads to hadith Sahih, Hasan, ..etc
//      gradsCounter += result.grade -> 1
    }
  }

  override def chapters(limit: Int)(implicit ec: ExecutionContext): Future[Seq[Counter]] = Future.successful(
    chaptersCounter.take(limit).toSeq.map {
      case (name, count) => Counter(name, count)
    })

  override def books(limit: Int)(implicit ec: ExecutionContext): Future[Seq[Counter]] = Future.successful(
    booksCounter.take(limit).toSeq.map {
      case (name, count) => Counter(name, count)
    })

  override def grads(limit: Int)(implicit ec: ExecutionContext): Future[Seq[Counter]] = Future.successful(
    gradsCounter.take(limit).toSeq.map {
      case (name, count) => Counter(name, count)
    })
}
