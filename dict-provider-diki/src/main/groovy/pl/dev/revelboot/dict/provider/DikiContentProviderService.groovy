package pl.dev.revelboot.dict.provider

import org.apache.http.client.methods.CloseableHttpResponse
import org.apache.http.client.methods.HttpGet
import org.apache.http.client.utils.URIBuilder
import org.apache.http.impl.client.HttpClients
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

import java.util.concurrent.Future

import static java.util.concurrent.CompletableFuture.supplyAsync

class DikiContentProviderService implements ContentProviderService {

    private static final PROVIDER_ID = "www.diki.pl"
    private static final URL_BASE_ADDRESS = "https://www.diki.pl/slownik-angielskiego"
    private static final BASE_ADDRESS = "https://www.diki.pl"

    private static final CSS_DEFINITION = ".foreignToNativeMeanings .hw"
    private static final CSS_AUDIO_ELEMENT = ".diki-results-left-column h1 .soundOnClick"
    private static final ATTR_DATA_AUDIO_URL = "data-audio-url"

    private def httpClient = HttpClients.createDefault()

    @Override
    Future<ContentProviderResponse> request(String query) throws ContentProviderException {

        def async = supplyAsync({

            def uri = new URIBuilder(URL_BASE_ADDRESS).setParameter("q", query).build()

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
                def definitions = doc.select(CSS_DEFINITION).collect { elm ->
                    elm.text()
                }

                def urlAudioStreams = doc.select(CSS_AUDIO_ELEMENT).collect { elm ->
                    elm.getElementsByAttribute(ATTR_DATA_AUDIO_URL).collect {

                        new URLAudioStream(
                                "$BASE_ADDRESS${it.attr(ATTR_DATA_AUDIO_URL)}",
                                query)
                    }
                }.flatten().unique()

                return new ContentProviderResponse(PROVIDER_ID, uri.toString(), query, "", definitions, [], urlAudioStreams, [])
            }

            return null
        })
        return async
    }


    static void main(String[] args) {
        def responseFuture = new DikiContentProviderService().request("invert")

        def contentProviderResponse = responseFuture.get()
        println(contentProviderResponse)
    }

}
