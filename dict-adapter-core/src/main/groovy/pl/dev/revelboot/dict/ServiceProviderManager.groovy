package pl.dev.revelboot.dict

import pl.dev.revelboot.dict.provider.impl.OxfordContentProviderService
import pl.dev.revelboot.dict.provider.impl.VocabularyContentProviderService
import pl.dev.revelboot.dict.provider.ContentProviderService
import pl.dev.revelboot.dict.provider.impl.DikiContentProviderService

class ServiceProviderManager {


    private static final List<ContentProviderService> services

    static {
        services = Arrays.asList(
                new DikiContentProviderService(),
                new OxfordContentProviderService(),
                new VocabularyContentProviderService()).asImmutable()
    }

    List<ContentProviderService> getProviders() {
        return services
    }
}
