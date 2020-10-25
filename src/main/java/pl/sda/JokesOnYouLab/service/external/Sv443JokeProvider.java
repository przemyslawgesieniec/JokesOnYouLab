package pl.sda.JokesOnYouLab.service.external;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import pl.sda.JokesOnYouLab.model.Joke;
import pl.sda.JokesOnYouLab.model.JokeException;

import java.util.HashSet;
import java.util.Set;

public class Sv443JokeProvider implements ExternalJokeProvider {

    public static final String SERVICE_IS_UNAVAILABLE = "Service is unavailable";
    public static final String RESPONSE_BODY_IS_NOT_A_VALID_JSON = "response body is not a valid Json";
    private static final String baseUrl = "https://sv443.net/jokeapi/v2";
    private static final String anyJokePath = "/joke/Any";
    private static final String jokeCategoriesPath = "/categories";
    private RestTemplate restTemplate;
    private ObjectMapper objectMapper;

    public Sv443JokeProvider(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public Joke getRandomJoke() throws JokeException {
        final String body = getResponseBodyOrThrow(baseUrl + anyJokePath, String.class);
        return extractJoke(body);
    }

    @Override
    public Joke getRandomJokeFromCategory(String category) {
        return null;
    }

    @Override
    public Set<String> getAvailableCategories() {
        final String categoriesJson = getResponseBodyOrThrow(baseUrl + jokeCategoriesPath, String.class);
        return extractCategories(categoriesJson);
    }

    private <T> T getResponseBodyOrThrow(final String url, final Class<T> aClass) throws JokeException {
        final ResponseEntity<T> responseEntity = restTemplate.getForEntity(url, aClass);

        if (responseEntity.getStatusCode() != HttpStatus.OK) {
            throw new JokeException(SERVICE_IS_UNAVAILABLE);
        }
        return responseEntity.getBody();
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
            throw new JokeException(RESPONSE_BODY_IS_NOT_A_VALID_JSON);
        }
    }

    private Set<String> extractCategories(final String responseBody) {
        try {
            final JsonNode jsonNode = objectMapper.readTree(responseBody);
            final ArrayNode categoriesArrayNode = (ArrayNode) (jsonNode.get("categories"));
            Set<String> categories = new HashSet<>();
            for (int i = 0; i < categoriesArrayNode.size(); i++) {
                categories.add(categoriesArrayNode.get(i).asText());
            }
            return categories;
        } catch (JsonProcessingException e) {
            throw new JokeException(RESPONSE_BODY_IS_NOT_A_VALID_JSON);
        }
    }
}
