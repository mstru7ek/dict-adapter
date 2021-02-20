package pl.dev.revelboot.dict.provider.impl

import org.apache.http.client.methods.CloseableHttpResponse
import org.apache.http.client.methods.HttpGet
import org.apache.http.client.utils.URIBuilder
import org.apache.http.impl.client.HttpClients
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import pl.dev.revelboot.dict.provider.ContentProviderResponse
import pl.dev.revelboot.dict.provider.ContentProviderService

import java.util.concurrent.Future

import static java.util.concurrent.CompletableFuture.supplyAsync

class GazetaProviderService implements ContentProviderService {

    private static final PROVIDER_ID = "www.diki.pl"
    private static final URL_BASE_ADDRESS = "https://www.gazeta.pl/0,0.html"

    private def httpClient = HttpClients.createDefault()

    @Override
    Future<ContentProviderResponse> request() {

        def async = supplyAsync({

            def uri = new URIBuilder(URL_BASE_ADDRESS).build()

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
                def definitions = doc.select(".headerOpening")
                def cresponse = ContentProviderResponse.builder()
                        .date(definitions.select(".headerOpening__date").text())
                        .title(definitions.select(".headerOpening__title").text())
                        .daily(definitions.select(".headerOpening__daily").text())
                        .build();
                return cresponse
            }
            return null
        })
        return async
    }

    static void main(String[] args) {
        def responseFuture = new GazetaProviderService().request()
        def contentProviderResponse = responseFuture.get()
        println(contentProviderResponse)
    }

}
