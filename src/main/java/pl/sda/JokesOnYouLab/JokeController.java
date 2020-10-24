package pl.sda.JokesOnYouLab;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import pl.sda.JokesOnYouLab.model.Joke;
import pl.sda.JokesOnYouLab.model.JokeException;
import pl.sda.JokesOnYouLab.service.JokeDispatcherService;

import java.util.Set;

@RestController
public class JokeController {

    private JokeDispatcherService jokeDispatcherService;

    public JokeController(JokeDispatcherService jokeDispatcherService) {
        this.jokeDispatcherService = jokeDispatcherService;
    }

    @GetMapping("/random")
    public ResponseEntity<Joke> getRandomJoke() throws JokeException {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(jokeDispatcherService.provideRandomJoke());
    }

    @GetMapping("/categories")
    public ResponseEntity<Set<String>> getJokeCategory() throws JokeException {

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(jokeDispatcherService.provideJokeCategories());
    }

    @GetMapping("/{category}/random")
    public ResponseEntity<Joke> getJokeCategory(@PathVariable("category") final String category) {

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(jokeDispatcherService.provideRandomJoke(category));
    }
}
