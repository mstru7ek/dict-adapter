package pl.dev.revelboot.dict

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class DictAdapterServlet {

    private static final Logger log = LoggerFactory.getLogger(DictAdapterServlet)

    private ServiceProviderManager serviceProviderManager = new ServiceProviderManager()

    private static final Cache cache = new LRUCache(200)

    @GetMapping("/api")
    def findAllEntries(@RequestParam String query) {

        def bodyResponse = cache.get(query)
        if (bodyResponse == null) {
            bodyResponse = serviceProviderManager.getProviders().collect { service -> service.request(query) }.findResults { it?.get()}
            cache.put(query, bodyResponse)
            log.info("Update cache ... '${query}'")
        }

        return new ResponseEntity<>(bodyResponse, HttpStatus.OK)
   }
}
