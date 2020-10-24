package pl.sda.JokesOnYouLab;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;
import pl.sda.JokesOnYouLab.service.external.ChucknorrisJokeProvider;
import pl.sda.JokesOnYouLab.service.external.ExternalJokeProvider;
import pl.sda.JokesOnYouLab.service.external.Sv443JokeProvider;

import java.util.ArrayList;
import java.util.List;

@SpringBootApplication
public class JokesOnYouLabApplication {

    public static void main(String[] args) {
        SpringApplication.run(JokesOnYouLabApplication.class, args);
    }

    @Bean
    public RestTemplate provideRestTemplate() {
        return new RestTemplate();
    }

    @Bean
    public ObjectMapper provideObjectMapper() {
        return new ObjectMapper();
    }

    @Bean
    public List<ExternalJokeProvider> provideExternalJokeImplementations(final RestTemplate restTemplate,
                                                                         final ObjectMapper objectMapper) {
        List<ExternalJokeProvider> list = new ArrayList<>();
        list.add(new Sv443JokeProvider(restTemplate, objectMapper));
        list.add(new ChucknorrisJokeProvider(restTemplate, objectMapper));
        return list;
    }
}
