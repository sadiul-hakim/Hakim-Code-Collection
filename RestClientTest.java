import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClient;

import java.nio.charset.StandardCharsets;
import java.util.Map;

public class RestClientTest {

    public static void main(String[] args) {

        try {
            var client = RestClient.create();

            String body = client.get()
                    .uri("https://jsonplaceholder.typicode.com/users/1")
                    .accept(MediaType.APPLICATION_JSON)
                    .acceptCharset(StandardCharsets.UTF_8)
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, (req, res) -> {
                        throw new HttpClientErrorException(res.getStatusCode(), "Client Error");
                    })
                    .onStatus(HttpStatusCode::is5xxServerError, (req, res) -> {
                        throw new HttpServerErrorException(res.getStatusCode(), "Server Error");
                    })
                    .body(String.class);

            System.out.println(body);

            Post body1 = client.get()
                    .uri("https://jsonplaceholder.typicode.com/posts/{id}", 1)
                    .retrieve()
                    .body(Post.class);
            System.out.println(body1);

            String body2 = client.get()
                    .uri("")
                    .header("Token", "")
                    .header("accessToken", "Bearer ")
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, (req, res) -> {
                        throw new HttpClientErrorException(res.getStatusCode(), "Client Error");
                    })
                    .onStatus(HttpStatusCode::is5xxServerError, (req, res) -> {
                        throw new HttpServerErrorException(res.getStatusCode(), "Server Error");
                    })
                    .body(String.class);
            System.out.println(body2);

//            String requestBody = """
//                    {
//                      "id": 1,
//                      "name": "Leanne Graham",
//                      "username": "Bret",
//                      "email": "Sincere@april.biz",
//                      "address": {
//                        "street": "Kulas Light",
//                        "suite": "Apt. 556",
//                        "city": "Gwenborough",
//                        "zipcode": "92998-3874",
//                        "geo": {
//                          "lat": "-37.3159",
//                          "lng": "81.1496"
//                        }
//                      },
//                      "phone": "1-770-736-8031 x56442",
//                      "website": "hildegard.org",
//                      "company": {
//                        "name": "Romaguera-Crona",
//                        "catchPhrase": "Multi-layered client-server neural-net",
//                        "bs": "harness real-time e-markets"
//                      }
//                    }
//                    """;
//            String body1 = client.post()
//                    .uri("https://jsonplaceholder.typicode.com/users")
//                    .contentType(MediaType.APPLICATION_JSON)
//                    .body(requestBody)
//                    .retrieve()
//                    .body(String.class);
//            System.out.println(body1);
//
//            String body2 = client.put()
//                    .uri("https://jsonplaceholder.typicode.com/posts/{id}", 1)
//                    .contentType(MediaType.APPLICATION_JSON)
//                    .body(Map.of("title", "Updated Title", "body", "Updated content", "userId", 1))
//                    .retrieve()
//                    .body(String.class);
//            System.out.println(body2);
//
//            ResponseEntity<Void> entity = client.delete()
//                    .uri("https://jsonplaceholder.typicode.com/posts/{id}", 1)
//                    .retrieve()
//                    .toBodilessEntity();
//            System.out.println(entity.getStatusCode().value());
        } catch (HttpClientErrorException | HttpServerErrorException ex) {
            System.out.println(ex.getMessage());
        }
    }
}

record Post(
        int id,
        String title,
        String body
) {
