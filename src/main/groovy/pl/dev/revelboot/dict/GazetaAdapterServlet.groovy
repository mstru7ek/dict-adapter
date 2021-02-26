package pl.dev.revelboot.dict

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import pl.dev.revelboot.dict.provider.ContentProviderService
import pl.dev.revelboot.dict.provider.impl.GazetaProviderService


// http://localhost:8080/api


@RestController
class GazetaAdapterServlet {

    private static final Logger log = LoggerFactory.getLogger(GazetaAdapterServlet)

    private ContentProviderService contentProvider = new GazetaProviderService()

    @GetMapping("/api")
    def grabContent() {
        def response = contentProvider.request().get();

        def message = "${response.getDate()} ; ${response.getTitle()} ; ${response.getDaily()}"

        SandMail.message("mstru7ek@gmail.com", "gazeta.pl", message);
        return new ResponseEntity<>(response, HttpStatus.OK)
   }
}
