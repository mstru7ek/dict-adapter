package pl.dev.revelboot.dict

import pl.dev.revelboot.dict.provider.ContentProviderResponse

interface Cache {

    List<ContentProviderResponse> get(String key);

    void put(String key, List<ContentProviderResponse> value)
}