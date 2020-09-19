package com.mstruzek

import com.mstruzek.dict.{ContentResponse, UrlAudio, UrlImage}
import spray.json.{DefaultJsonProtocol, JsonFormat}

object AppJsonProtocol extends DefaultJsonProtocol {
  implicit val urlImageFormat: JsonFormat[UrlImage] = jsonFormat2(UrlImage)
  implicit val urlAudioFormat: JsonFormat[UrlAudio] = jsonFormat2(UrlAudio)
  implicit val contentResponseFormat: JsonFormat[ContentResponse] = jsonFormat8(ContentResponse)
}
