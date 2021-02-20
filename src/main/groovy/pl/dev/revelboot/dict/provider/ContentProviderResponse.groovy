package pl.dev.revelboot.dict.provider


import groovy.transform.ToString
import groovy.transform.builder.Builder

@ToString
@Builder
class ContentProviderResponse {
    String date
    String title
    String daily
}
