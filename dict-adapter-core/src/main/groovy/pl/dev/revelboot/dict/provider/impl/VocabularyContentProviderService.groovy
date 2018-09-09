package pl.dev.revelboot.dict.provider.impl

import org.apache.http.client.methods.CloseableHttpResponse
import org.apache.http.client.methods.HttpGet
import org.apache.http.client.utils.URIBuilder
import org.apache.http.impl.client.HttpClients
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import pl.dev.revelboot.dict.provider.ContentProviderException
import pl.dev.revelboot.dict.provider.ContentProviderResponse
import pl.dev.revelboot.dict.provider.ContentProviderService
import pl.dev.revelboot.dict.provider.URLAudioStream

import java.util.concurrent.Future

import static java.util.concurrent.CompletableFuture.supplyAsync

class VocabularyContentProviderService implements ContentProviderService {

    private static final PROVIDER_ID = "www.vocabulary.com"
    private static final URL_BASE_ADDRESS = "https://www.vocabulary.com/dictionary/%s"
    private static final AUDIO_URL_FORMAT = "https://audio.vocab.com/1.0/us/%s.mp3"

    private static final String WORD_SEPARATOR = "%20"


    private static final CSS_DEFINITION = ".foreignToNativeMeanings .hw"
    private static final CSS_AUDIO_ELEMENT = ".diki-results-left-column h1 .soundOnClick"
    private static final ATTR_DATA_AUDIO_URL = "data-audio"

    private def httpClient = HttpClients.createDefault()

    @Override
    Future<ContentProviderResponse> request(String query) throws ContentProviderException {

        def async = supplyAsync({

            def queryParameter = query.replaceAll(" ", WORD_SEPARATOR)
            def webAddress = String.format(URL_BASE_ADDRESS, queryParameter)

            def uri = new URIBuilder(webAddress).build()

            def httpGet = new HttpGet(uri)
            CloseableHttpResponse response = httpClient.execute(httpGet)

            Document doc
            try {
                def entity = response.getEntity()
                if (entity != null) {
                    def inputStream = entity.content
                    try {
                        doc = Jsoup.parse(inputStream, null, "")
                    } finally {
                        inputStream.close()
                    }
                }
            } finally {
                response.close()
            }

            if (doc) {

                def description = doc.select(".page .definitionsContainer .short").text()

                def definitions = doc.select(".page .centeredContent .definitions h3.definition").collect { elm ->
                    elm.textNodes().join().trim()
                }

                def synonyms = doc.select(".page .centeredContent .definitions .instances").collect { elm ->
                    if (elm.text().contains("Synonyms:")) {
                        return elm.select(".word")?.eachText()
                    }
                    []
                }.flatten()

                def urlAudioStreams = doc.select(".page .centeredContent a.audio").collect { elm ->
                    elm.getElementsByAttribute(ATTR_DATA_AUDIO_URL).collect {
                        new URLAudioStream(
                                String.format(AUDIO_URL_FORMAT, it.attr(ATTR_DATA_AUDIO_URL)),
                                query)
                    }
                }.flatten().unique()

                return new ContentProviderResponse(PROVIDER_ID, uri.toString(), query, description, definitions, synonyms, urlAudioStreams, [])
            }

            return null
        })
        return async
    }


    static void main(String[] args) {
        def responseFuture = new VocabularyContentProviderService().request("invert")

        def contentProviderResponse = responseFuture.get()
        println(contentProviderResponse)
    }

}
