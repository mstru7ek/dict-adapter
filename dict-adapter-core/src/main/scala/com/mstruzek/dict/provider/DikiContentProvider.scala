package com.mstruzek.dict.provider

import com.mstruzek.dict.{ContentProvider, ContentResponse, UrlAudio}
import org.apache.http.client.methods.HttpGet
import org.apache.http.client.utils.URIBuilder
import org.apache.http.impl.client.HttpClients
import org.jsoup.Jsoup

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.DurationInt
import scala.jdk.CollectionConverters._
import scala.concurrent.{Await, Future}
import scala.util.{Failure, Success, Try, Using}

import scala.language.postfixOps

object DikiContentProvider extends ContentProvider {

  private val PROVIDER_ID = "www.diki.pl"
  private val URL_BASE_ADDRESS = "https://www.diki.pl/slownik-angielskiego"
  private val BASE_ADDRESS = "https://www.diki.pl"

  private val CSS_DEFINITION = ".foreignToNativeMeanings .hw"
  private val CSS_AUDIO_ELEMENT = ".diki-results-left-column h1 .soundOnClick"
  private val ATTR_DATA_AUDIO_URL = "data-audio-url"

  private val httpClient = HttpClients.createDefault()

  override def query(phrase: String): Future[ContentResponse] =
    Future {
      val uri = new URIBuilder(URL_BASE_ADDRESS).setParameter("q", phrase).build()
      val httpGet = new HttpGet(uri)

      val response = httpClient.execute(httpGet)

      Using.resource(response) { r =>
        Using.resource(r.getEntity.getContent) {
          c =>
            val document = Jsoup.parse(c, "UTF-8", "")

            val definitions = document.select(CSS_DEFINITION)
              .asScala
              .map(_.text)
              .toList

            val urlAudios = document.select(CSS_AUDIO_ELEMENT)
              .asScala
              .flatMap { elem =>
                elem.getElementsByAttribute(ATTR_DATA_AUDIO_URL)
                  .asScala
                  .map { attribute =>
                    UrlAudio(s"$BASE_ADDRESS${attribute.attr(ATTR_DATA_AUDIO_URL)}", phrase)
                  }
                  .toSet
              }
              .distinct
              .toList

            ContentResponse(
              PROVIDER_ID,
              uri.toString,
              phrase,
              "",
              definitions,
              List.empty,
              urlAudios,
              List.empty)
        }
      }
    }

  def main(args: Array[String]): Unit = {

    val eventualResponse: Future[ContentResponse] = DikiContentProvider.query("sleepy")

    Await.result(eventualResponse, 10 seconds)

    println(eventualResponse.value)
  }

  def showResponse(cr: ContentResponse): Unit = {
    println(cr)
  }

}
