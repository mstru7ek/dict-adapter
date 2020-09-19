package com.mstruzek

import scala.language.postfixOps
import com.mstruzek.dict.ContentResponse
import com.mstruzek.dict.provider.{DikiContentProvider, VocabularyContentProvider}

import scala.concurrent.{Await, Awaitable, Future}
import scala.concurrent.duration.DurationInt

object QueryService {

  val lru = new LRUSave[String, List[ContentResponse]](100)

  val providers = List(
    DikiContentProvider,
    VocabularyContentProvider
  )

  def search(query: Option[String]) : List[ContentResponse] = {
    query match {
      case None => List.empty
      case Some(q) =>
        val result = lru.get(q)
        if (result == null) {
          val computed = providers
            .map(_.query(q))
            .map(Await.result(_, 10 seconds))

          lru.put(q, computed)
        }
        lru.get(q)
    }
  }
}
