package com.mstruzek.dict.provider

import java.io.InputStream

import com.mstruzek.dict.{ContentProvider, ContentResponse, UrlAudio}
import org.apache.http.client.methods.HttpGet
import org.apache.http.client.utils.URIBuilder
import org.apache.http.impl.client.HttpClients
import org.jsoup.Jsoup

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.DurationInt
import scala.concurrent.{Await, Future}
import scala.jdk.CollectionConverters._
import scala.language.postfixOps
import scala.util.Using

object VocabularyContentProvider extends ContentProvider {

  private val PROVIDER_ID = "www.vocabulary.com"

  private val WORD_SEPARATOR = "%20"
  private val ATTR_DATA_AUDIO_URL = "data-audio"

  private val httpClient = HttpClients.createDefault()

  override def query(phrase: String): Future[ContentResponse] = Future {

    val queryParameter = phrase.replaceAll(" ", WORD_SEPARATOR)

    val uri = new URIBuilder(s"https://www.vocabulary.com/dictionary/${queryParameter}").build()

    val response = httpClient.execute(new HttpGet(uri))

    Using.resource(response) { r =>
      Using.resource(r.getEntity.getContent) { c: InputStream =>

        val document = Jsoup.parse(c, null, "")

        val description = document.select(".page .definitionsContainer .short")
          .text()

        val definitions = document.select(".page .centeredContent .definitions h3.definition")
          .asScala
          .filter( _ != null)
          .map(e => e.text())
          .toList

        val synonyms = document.select(".page .centeredContent .definitions .instances")
          .asScala
          .filter(elm => elm.text().contains("Synonyms:"))
          .flatMap(elm => elm.select(".word").eachText().asScala)
          .toList
          .asInstanceOf[List[String]]

        val urlAudioStreams = document.select(".page .centeredContent a.audio")
          .asScala
          .flatMap {
            elm =>
              elm.getElementsByAttribute(ATTR_DATA_AUDIO_URL).asScala.map { elm =>
                UrlAudio(s"https://audio.vocab.com/1.0/us/${elm.attr(ATTR_DATA_AUDIO_URL)}.mp3", phrase)
              }
          }
          .distinct
          .toList

        ContentResponse(
          PROVIDER_ID,
          uri.toString,
          phrase,
          description,
          definitions,
          synonyms,
          urlAudioStreams,
          List.empty)
      }
    }
  }

  def main(args: Array[String]): Unit = {
    val eventualResponse = VocabularyContentProvider.query("cognizant")

    Await.result(eventualResponse, 10 seconds)

    println(eventualResponse.value)
  }
}
