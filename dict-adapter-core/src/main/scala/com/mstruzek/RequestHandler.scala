package com.mstruzek

import com.mstruzek.AppJsonProtocol._
import javax.servlet.http.{HttpServletRequest, HttpServletResponse}
import org.eclipse.jetty.server.Request
import org.eclipse.jetty.server.handler.AbstractHandler
import spray.json._

class RequestHandler extends AbstractHandler {

  override def handle(target: String,baseRequest: Request, request: HttpServletRequest,
                      response: HttpServletResponse): Unit = {
    if (target == "/favicon.ico") {
      baseRequest.setHandled(true)
      return
    }

    val list = QueryService.search(Option(request.getParameter("query")))

    response.setStatus(200)
    response.setContentType("application/json")
    response.getWriter.println(list.toJson)
    baseRequest.setHandled(true)
  }


}
