package com.mstruzek.dict.provider

import scala.language.postfixOps

import com.jayway.jsonpath.Option.DEFAULT_PATH_LEAF_TO_NULL
import com.jayway.jsonpath.spi.json.JacksonJsonProvider
import com.jayway.jsonpath.spi.mapper.JacksonMappingProvider
import com.jayway.jsonpath.{Configuration, JsonPath}
import com.mstruzek.dict.{ContentProvider, ContentResponse, UrlAudio, UrlImage}
import org.apache.http.client.methods.HttpGet
import org.apache.http.client.utils.URIBuilder
import org.apache.http.impl.client.HttpClients

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.DurationInt
import scala.concurrent.{Await, Future}
import scala.util.Using

object OxfordContentProviderService extends ContentProvider {

  private val OXFORD_ENDPOINT_URL = "https://od-api.oxforddictionaries.com:443/api/v2/"

  private val OXFORD_APP_ID = ""
  private val OXFORD_APP_KEY = ""
  private val PROVIDER_ID = "en.oxforddictionaries.com"

  private val httpClient = HttpClients.createDefault()

  private val configuration = Configuration.builder()
    .jsonProvider(new JacksonJsonProvider()).mappingProvider(new JacksonMappingProvider()).options(DEFAULT_PATH_LEAF_TO_NULL)
    .build()


  override def query(phrase: String): Future[ContentResponse] = {

    val source_lang = "en-gb"
    val word_id = phrase.replaceAll(" ", "_")

    val asyncSynonyms =
      Future {
        val requestURI = new URIBuilder(s"$OXFORD_ENDPOINT_URL/entries/$source_lang/$word_id/synonyms").build()
        val httpGet = new HttpGet(requestURI)
        httpGet.addHeader("app_id", OXFORD_APP_ID)
        httpGet.addHeader("app_key", OXFORD_APP_KEY)

        Using.resource(httpClient.execute(httpGet)) {
          response =>
            response.getStatusLine.getStatusCode match {
              case 200 =>
                Some(JsonPath.using(configuration).parse(response.getEntity.getContent))
              case _ =>
                None
            }
        }
      }

    val asyncEntries =
      Future {
        val requestURI = new URIBuilder(s"$OXFORD_ENDPOINT_URL/entries/$source_lang/$word_id").build()
        val httpGet = new HttpGet(requestURI)
        httpGet.addHeader("app_id", OXFORD_APP_ID)
        httpGet.addHeader("app_key", OXFORD_APP_KEY)

        Using.resource(httpClient.execute(httpGet)) {
          response =>
            response.getStatusLine.getStatusCode match {
              case 200 =>
                Some(JsonPath.using(configuration).parse(response.getEntity.getContent))
              case _ =>
                None
            }
        }
      }

    for (
      n <- asyncEntries;
      s <- asyncSynonyms
    ) yield {
      n match {
        case None =>
          throw new IllegalStateException("no entries")

        case Some(entries) =>

          type TAggregate = List[String]

          val synonyms: List[String] = s.map(
            syn =>
              List(
                syn.read("$.results[*].lexicalEntries[*].entries[*].senses[*].synonyms[*].text", classOf[TAggregate]),
                syn.read("$.results[*].lexicalEntries[*].entries[*].senses[*].subsenses[*].synonyms[*].text", classOf[TAggregate])
              ).flatten
          ).orNull


          val definitions: List[String] = List(
            entries.read("$.results[*].lexicalEntries[*].entries[*].senses[*].definitions[*]'", classOf[TAggregate]),
            entries.read("$.results[*].lexicalEntries[*].entries[*].senses[*].short_definitions[*]", classOf[TAggregate]),
            entries.read("$.results[*].lexicalEntries[*].entries[*].senses[*].subsenses[*].definitions[*]", classOf[TAggregate]),
            entries.read("$.results[*].lexicalEntries[*].entries[*].senses[*].subsenses[*].short_definitions[*]", classOf[TAggregate])
          ).flatten

          val urlAudios = entries.read("$.results[*].lexicalEntries[*].pronunciations[*].audioFile", classOf[TAggregate])
            .filter(_ != null)
            .map(UrlAudio(_, phrase))

          ContentResponse(PROVIDER_ID, OXFORD_ENDPOINT_URL, phrase, "", definitions, synonyms, urlAudios, List.empty[UrlImage])
      }
    }
  }

  def main(args: Array[String]): Unit = {


    val responseFuture = OxfordContentProviderService.query("infatuate")

    Await.result(responseFuture, 10 seconds)

    println(responseFuture.value)

  }
}




