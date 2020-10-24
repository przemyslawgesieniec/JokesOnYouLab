package pl.sda.JokesOnYouLab.service.external;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import pl.sda.JokesOnYouLab.model.Joke;
import pl.sda.JokesOnYouLab.model.JokeException;

import java.util.HashSet;
import java.util.Set;

public class Sv443JokeProvider implements ExternalJokeProvider {

    private static final String baseUrl = "https://sv443.net/jokeapi/v2";
    private static final String anyJokePath = "/joke/Any";

    private RestTemplate restTemplate;
    private ObjectMapper objectMapper;

    public Sv443JokeProvider(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public Joke getRandomJoke() throws JokeException {
        final ResponseEntity<String> responseEntity = restTemplate.getForEntity(baseUrl + anyJokePath, String.class);

        if (responseEntity.getStatusCode() != HttpStatus.OK) {
            throw new JokeException("service is unavailable");
        }
        final String body = responseEntity.getBody();
        return extractJoke(body);
    }

    @Override
    public Joke getRandomJokeFromCategory(String category) {
        return null;
    }

    @Override
    public Set<String> getAvailableCategories() {
        return new HashSet<>();
    }

    private Joke extractJoke(final String responseBody) {
        try {
            final JsonNode jsonNode = objectMapper.readTree(responseBody);
            final String category = jsonNode.path("category").asText();
            String joke = jsonNode.path("joke").asText();
            if (joke.isEmpty()) {
                final String setup = jsonNode.path("setup").asText();
                final String delivery = jsonNode.path("delivery").asText();
                joke = setup + "\n" + delivery;
            }
            return new Joke(joke, category);
        } catch (JsonProcessingException e) {
            throw new JokeException("response body is not a valid Json");
        }
    }
}
