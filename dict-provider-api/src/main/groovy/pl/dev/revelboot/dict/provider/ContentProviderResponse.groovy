package pl.dev.revelboot.dict.provider

import groovy.transform.Immutable
import groovy.transform.ToString

@Immutable
@ToString
class ContentProviderResponse {

    String providerId
    String requestUrl
    String query
    String description
    List<String> definitions
    List<String> synonyms
    List<URLAudioStream> urlAudioStreams
    List<URLImageStream> urlImageStreams
}
