package pl.sda.JokesOnYouLab.service.external;

import pl.sda.JokesOnYouLab.model.Joke;
import pl.sda.JokesOnYouLab.model.JokeException;

import java.util.Set;

public interface ExternalJokeProvider {

    Joke getRandomJoke() throws JokeException;

    Joke getRandomJokeFromCategory(final String category) throws JokeException;

    Set<String> getAvailableCategories() throws JokeException;
}
