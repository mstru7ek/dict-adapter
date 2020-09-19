package com.mstruzek

import org.eclipse.jetty.server.Server

object AppEntryPoint {

  def main(args: Array[String]) : Unit = {

    val server = new Server(9090)
    server.setHandler(new RequestHandler)
    server.start()

    println("working")
    server.join()
  }

}
