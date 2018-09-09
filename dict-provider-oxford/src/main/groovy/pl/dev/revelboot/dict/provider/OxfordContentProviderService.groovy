package pl.dev.revelboot.dict.provider

import com.fasterxml.jackson.databind.ObjectMapper
import groovy.util.logging.Log4j
import org.apache.http.client.methods.CloseableHttpResponse
import org.apache.http.client.methods.HttpGet
import org.apache.http.client.utils.URIBuilder
import org.apache.http.impl.client.HttpClients

import java.util.concurrent.Future

import static java.util.concurrent.CompletableFuture.supplyAsync

class OxfordContentProviderService implements ContentProviderService {

    public static final String OXFORD_ENDPOINT_URL = "https://od-api.oxforddictionaries.com/api/v1"
    public static final ObjectMapper mapper = new ObjectMapper()

    public static final String OXFORD_APP_ID = "6b300d5f"
    public static final String OXFORD_APP_KEY = "be5ef236f8aba9e416abb51e27960ea0"
    public static final String PROVIDER_ID = "en.oxforddictionaries.com"

    private def httpClient = HttpClients.createDefault()

    @Override
    Future<ContentProviderResponse> request(String query) throws ContentProviderException {

        def source_lang = "en"
        def word_id = query.replaceAll(" ", "_")

        def asyncSynonyms = supplyAsync {

            def requestURI = new URIBuilder(OXFORD_ENDPOINT_URL + "/entries/${source_lang}/${word_id}/synonyms").build()

            def httpGet = new HttpGet(requestURI)
            httpGet.addHeader("app_id", OXFORD_APP_ID)
            httpGet.addHeader("app_key", OXFORD_APP_KEY)

            CloseableHttpResponse response = httpClient.execute(httpGet)
            try {
                if (response.statusLine.statusCode != 200) {
                    println("Invalid response: ${response.statusLine.statusCode }")
                    return null
                }

                def content = response.getEntity().content.text
                Thesaurus thesaurus = mapper.readValue(content, Thesaurus.class)
                return thesaurus

            } finally {
                response.close()
            }
        }

        def asyncEntries = supplyAsync({

            def requestURI = new URIBuilder(OXFORD_ENDPOINT_URL + "/entries/${source_lang}/${word_id}").build()

            def httpGet = new HttpGet(requestURI)
            httpGet.addHeader("app_id", OXFORD_APP_ID)
            httpGet.addHeader("app_key", OXFORD_APP_KEY)

            CloseableHttpResponse response = httpClient.execute(httpGet)
            try {
                if (response.statusLine.statusCode != 200) {
                    println("Invalid response: ${response.statusLine.statusCode }")
                    return null
                }

                def content = response.getEntity().content.text
                RetrieveEntry retrieveEntry = mapper.readValue(content, RetrieveEntry.class)
                return retrieveEntry
            } finally {
                response.close()
            }
        })

        def async = asyncEntries.thenCombineAsync(asyncSynonyms, { retrieveEntry, thesaurus ->

            if (retrieveEntry == null || thesaurus == null) {
                return null
            }

            List<String> definitions = retrieveEntry.results*.lexicalEntries.flatten()
                    .collect { it.entries }.flatten()
                    .findResults { it.senses }.flatten()
                    .findResults { [it.definitions, it.short_definitions, it.subsenses*.definitions, it.subsenses*.short_definitions].flatten() }
                    .flatten().findResults { it }

            List<URLAudioStream> urlAudioStreams = retrieveEntry.results*.lexicalEntries.flatten()
                    .findResults { it.pronunciations }.flatten()
                    .findResults { new URLAudioStream(it.audioFile, query)}

            List<String> synonyms = thesaurus.results*.lexicalEntries.flatten()
                    .collect { it.entries }.flatten()
                    .collect { it.senses }.flatten()
                    .findResults { it.synonyms }.flatten()
                    .findResults { it.text }

            List<String> subSynonyms = thesaurus.results*.lexicalEntries.flatten()
                    .collect { it.entries }.flatten()
                    .collect { it.senses }.flatten()
                    .findResults { it.subsenses }.flatten()
                    .findResults { it.synonyms }.flatten()
                    .findResults { it.text }

            synonyms?.addAll(subSynonyms)

            return new ContentProviderResponse(PROVIDER_ID, OXFORD_ENDPOINT_URL, query, "", definitions, synonyms, urlAudioStreams, [])
        })
        return async
    }


    static void main(String[] args) {
//        def responseFuture = new OxfordContentProviderService().request("invert")
        def responseFuture = new OxfordContentProviderService().request("could")

        def contentProviderResponse = responseFuture.get()
        println(contentProviderResponse)


    }

}

