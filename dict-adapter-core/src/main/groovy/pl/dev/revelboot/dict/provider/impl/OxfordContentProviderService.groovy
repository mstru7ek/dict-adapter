package pl.dev.revelboot.dict.provider.impl

import com.jayway.jsonpath.Configuration
import com.jayway.jsonpath.JsonPath
import com.jayway.jsonpath.spi.json.JacksonJsonProvider
import com.jayway.jsonpath.spi.mapper.JacksonMappingProvider
import org.apache.http.client.methods.CloseableHttpResponse
import org.apache.http.client.methods.HttpGet
import org.apache.http.client.utils.URIBuilder
import org.apache.http.impl.client.HttpClients
import pl.dev.revelboot.dict.provider.ContentProviderException
import pl.dev.revelboot.dict.provider.ContentProviderResponse
import pl.dev.revelboot.dict.provider.ContentProviderService
import pl.dev.revelboot.dict.provider.URLAudioStream

import java.util.concurrent.Future

import static com.jayway.jsonpath.Option.DEFAULT_PATH_LEAF_TO_NULL
import static java.util.concurrent.CompletableFuture.supplyAsync

class OxfordContentProviderService implements ContentProviderService {

    private static final String OXFORD_ENDPOINT_URL = "https://od-api.oxforddictionaries.com/api/v1"

    private static final String OXFORD_APP_ID = "6b300d5f"
    private static final String OXFORD_APP_KEY = "be5ef236f8aba9e416abb51e27960ea0"
    private static final String PROVIDER_ID = "en.oxforddictionaries.com"

    private def httpClient = HttpClients.createDefault()

    private static final configuration = Configuration.builder()
            .jsonProvider(new JacksonJsonProvider()).mappingProvider(new JacksonMappingProvider()).options(DEFAULT_PATH_LEAF_TO_NULL)
            .build()

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
                return JsonPath.using(configuration).parse(response.getEntity().content)
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
                return JsonPath.using(configuration).parse(response.getEntity().content)
            } finally {
                response.close()
            }
        })

        def async = asyncEntries.thenCombineAsync(asyncSynonyms, { retrieveEntry, thesaurus ->

            if (retrieveEntry == null || thesaurus == null) {
                return null
            }

            List<String> synonyms  = Arrays.asList(
                    thesaurus.read('$.results[*].lexicalEntries[*].entries[*].senses[*].synonyms[*].text'),
                    thesaurus.read('$.results[*].lexicalEntries[*].entries[*].senses[*].subsenses[*].synonyms[*].text')
            ).flatten()

            List<String> definitions = Arrays.asList(
                    retrieveEntry.read('$.results[*].lexicalEntries[*].entries[*].senses[*].definitions[*]'),
                    retrieveEntry.read('$.results[*].lexicalEntries[*].entries[*].senses[*].short_definitions[*]'),
                    retrieveEntry.read('$.results[*].lexicalEntries[*].entries[*].senses[*].subsenses[*].definitions[*]'),
                    retrieveEntry.read('$.results[*].lexicalEntries[*].entries[*].senses[*].subsenses[*].short_definitions[*]')
            ).flatten()

            List<URLAudioStream> urlAudioStreams = retrieveEntry.read('$.results[*].lexicalEntries[*].pronunciations[*].audioFile')
                    .collect { new URLAudioStream(it, query) }

            return new ContentProviderResponse(PROVIDER_ID, OXFORD_ENDPOINT_URL, query, "", definitions, synonyms, urlAudioStreams, [])
        })
        return async
    }


    static void main(String[] args) {
//        def responseFuture = new OxfordContentProviderService().request("invert")
        def responseFuture = new OxfordContentProviderService().request("invert")

        def contentProviderResponse = responseFuture.get()
        println(contentProviderResponse)


    }

}

