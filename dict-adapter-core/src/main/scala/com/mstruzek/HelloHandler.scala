package com.mstruzek

import com.mstruzek.MyJsonProtocol._
import com.mstruzek.dict.ContentResponse
import com.mstruzek.dict.provider.{DikiContentProvider, VocabularyContentProvider}
import javax.servlet.http.{HttpServletRequest, HttpServletResponse}
import org.eclipse.jetty.server.Request
import org.eclipse.jetty.server.handler.AbstractHandler
import spray.json._

import scala.concurrent.Await
import scala.concurrent.duration.DurationInt

class HelloHandler extends AbstractHandler {

  val lru = new LRUSave[String, List[ContentResponse]](100)

  val providers = List(
    DikiContentProvider,
    VocabularyContentProvider
  )

  override def handle(target: String,baseRequest: Request, request: HttpServletRequest,
                      response: HttpServletResponse): Unit = {
    if (target == "/favicon.ico") {
      baseRequest.setHandled(true)
      return
    }

    val list = search(Option(request.getParameter("query")))

    response.setStatus(200)
    response.setContentType("application/json")
    response.getWriter.println(list.toJson)
    baseRequest.setHandled(true)
  }

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
