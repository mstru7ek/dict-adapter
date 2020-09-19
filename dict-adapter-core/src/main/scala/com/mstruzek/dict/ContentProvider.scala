package com.mstruzek.dict

import scala.concurrent.Future

trait ContentProvider {

  /**
   * Request for phrase in dictionary service.
   * @param phrase query phrase
   * @return future content response
   */
  def query(phrase: String) : Future[ContentResponse]
}
