package pl.dev.revelboot.dict

import com.fasterxml.jackson.databind.ObjectMapper
import pl.dev.revelboot.dict.provider.ContentProviderResponse

import javax.servlet.ServletConfig
import javax.servlet.ServletException
import javax.servlet.annotation.WebServlet
import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets

@WebServlet(urlPatterns = ['/api'])
class DictAdapterServlet extends HttpServlet {

    private ServiceProviderManager serviceProviderManager = new ServiceProviderManager()

    @Override
    void init(ServletConfig config) throws ServletException {
        super.init(config)
        serviceProviderManager.init()
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        String query = req.parameterMap['query']?.first()

        def mapper = new ObjectMapper()

        List<ContentProviderResponse> body = serviceProviderManager.getProviders()
                .collect { service -> service.request(query) }
                .findResults { it?.get()}

        def responseBody = mapper.writeValueAsString(body)

        resp.setStatus(200)
        resp.setCharacterEncoding("UTF-8")
        resp.setContentType("application/json")
        resp.addHeader("Access-Control-Allow-Origin","*")

        if (responseBody) {
            resp.setContentLength(responseBody.getBytes("UTF-8").length)

            resp.writer.write(responseBody)
            resp.writer.flush()
        }
   }
}
