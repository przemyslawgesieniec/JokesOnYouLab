package pl.sda.JokesOnYouLab.service;

import org.springframework.stereotype.Service;
import pl.sda.JokesOnYouLab.model.Joke;
import pl.sda.JokesOnYouLab.model.JokeException;
import pl.sda.JokesOnYouLab.service.external.ExternalJokeProvider;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class JokeDispatcherService {

    private List<ExternalJokeProvider> externalJokeProviderList;

    public JokeDispatcherService(List<ExternalJokeProvider> externalJokeProviderList) {
        this.externalJokeProviderList = externalJokeProviderList;
    }

    public Joke provideRandomJoke() throws JokeException {
        return externalJokeProviderList.get(1).getRandomJoke();
    }

    public Joke provideRandomJoke(final String category) {

        return new Joke("asd", category);
    }

    public Set<String> provideJokeCategories() {
        return externalJokeProviderList.stream()
                .map(ExternalJokeProvider::getAvailableCategories)
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());
    }
}
