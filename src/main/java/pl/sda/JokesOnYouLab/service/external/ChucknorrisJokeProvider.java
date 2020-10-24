package pl.sda.JokesOnYouLab.service.external;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import pl.sda.JokesOnYouLab.model.Joke;
import pl.sda.JokesOnYouLab.model.JokeException;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class ChucknorrisJokeProvider implements ExternalJokeProvider {

    private static final String baseUrl = "https://api.chucknorris.io";
    private static final String anyJokeFromCategoryPath = "/jokes/random?category=";
    private static final String categoryPath = "/jokes/categories";

    private RestTemplate restTemplate;
    private ObjectMapper objectMapper;

    public ChucknorrisJokeProvider(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public Joke getRandomJoke() throws JokeException {
        final String randomCategory = chooseRandomCategory();
        return getRandomJokeFromCategory(randomCategory);
    }

    @Override
    public Joke getRandomJokeFromCategory(String category) throws JokeException {
        final String body = getResponseBodyOrThrowException(baseUrl + anyJokeFromCategoryPath + category, String.class);

        final JsonNode jsonNode;

        try {
            jsonNode = objectMapper.readTree(body);
        } catch (JsonProcessingException e) {
            throw new JokeException("response body is not a valid Json");
        }

        String value = jsonNode.path("value").asText();
        return new Joke(value, category);
    }

    @Override
    public Set<String> getAvailableCategories() throws JokeException {
        final String[] responseBodyOrThrowException = getResponseBodyOrThrowException(baseUrl + categoryPath, String[].class);
        return new HashSet<>(Arrays.asList(responseBodyOrThrowException));
    }

    private <T> T getResponseBodyOrThrowException(final String url, Class<T> aClass) throws JokeException {
        HttpHeaders headers = new HttpHeaders();

        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("user-agent", "JokenOnYOuApplication");
        HttpEntity entity = new HttpEntity(headers);

        final ResponseEntity<T> exchange = restTemplate.exchange(
                url, HttpMethod.GET, entity, aClass);
        if (!exchange.getStatusCode().equals(HttpStatus.OK)) {
            throw new JokeException("External service did not respond as expected");
        }
        return exchange.getBody();
    }

    private String chooseRandomCategory() throws JokeException {
        final Set<String> availableCategories = getAvailableCategories();
        return availableCategories
                .stream()
                .skip(new Random().nextInt(availableCategories.size()))
                .findFirst()
                .orElse("Chuck Norris");
    }
}
