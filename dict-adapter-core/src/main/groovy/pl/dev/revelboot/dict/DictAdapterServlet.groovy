package pl.dev.revelboot.dict

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import pl.dev.revelboot.dict.provider.ContentProviderResponse

@RestController
class DictAdapterServlet {

    private static final Logger log = LoggerFactory.getLogger(DictAdapterServlet)

    private ServiceProviderManager serviceProviderManager = new ServiceProviderManager()

    @GetMapping("/api")
    def findAllEntries(@RequestParam String query) {

         List<ContentProviderResponse> body = serviceProviderManager.getProviders()
                .collect { service -> service.request(query) }
                .findResults { it?.get()}


        return new ResponseEntity<>(body, HttpStatus.OK)
   }
}
