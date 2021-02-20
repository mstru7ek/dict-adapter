package pl.dev.revelboot.dict.provider

import java.util.concurrent.Future

interface ContentProviderService {

    Future<ContentProviderResponse> request()
}