package com.mstruzek.dict

case class ContentResponse(providerId: String,
                           requestUrl: String,
                           query: String,
                           description: String,
                           definitions: List[String],
                           synonyms: List[String],
                           urlAudios: List[UrlAudio],
                           urlImages: List[UrlImage]) {

  override def toString: String = {
    s"""
       |ContentResponse {
       |  providerId = $providerId
       |  requestUrl = $requestUrl
       |  query = $query
       |  description = $description
       |  definitions = $definitions
       |  synonyms = $synonyms
       |  urlAudios = $urlAudios
       |  urlImages = $urlImages
       |}
       |""".stripMargin
  }
}



