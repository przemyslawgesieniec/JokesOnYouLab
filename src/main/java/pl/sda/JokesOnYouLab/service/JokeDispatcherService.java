package pl.sda.JokesOnYouLab.service;

import org.springframework.stereotype.Service;
import pl.sda.JokesOnYouLab.model.Joke;
import pl.sda.JokesOnYouLab.model.JokeException;
import pl.sda.JokesOnYouLab.service.external.ExternalJokeProvider;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class JokeDispatcherService {

    private List<ExternalJokeProvider> externalJokeProviderList;
    private RandomnessProvider randomnessProvider;

    public JokeDispatcherService(final List<ExternalJokeProvider> externalJokeProviderList,
                                 final RandomnessProvider randomnessProvider) {
        this.externalJokeProviderList = externalJokeProviderList;
        this.randomnessProvider = randomnessProvider;
    }

    public Joke provideRandomJoke() throws JokeException {
        final List<Integer> callingOrder = randomnessProvider.determineCallingOrder(externalJokeProviderList.size());
        return getJokeOrCallAnotherServiceInCaseOfError(callingOrder);
    }

    public Joke provideRandomJoke(final String category) {

        return new Joke("asd", category);
    }

    public Set<String> provideJokeCategories() throws JokeException {
        Set<String> set = new HashSet<>();
        for (ExternalJokeProvider externalJokeProvider : externalJokeProviderList) {
            Set<String> availableCategories = externalJokeProvider.getAvailableCategories();
            for (String string : availableCategories) {
                set.add(string);
            }
        }
        return set;
    }


    //  1
    private Joke getJokeOrCallAnotherServiceInCaseOfError(List<Integer> callingOrder) throws JokeException {
        try {
            return externalJokeProviderList.get(callingOrder.get(0)).getRandomJoke();
        } catch (JokeException e) {
            callingOrder.remove(0);
            if (callingOrder.size() == 0) {
                throw new JokeException("All services are unavailable");
            }
            getJokeOrCallAnotherServiceInCaseOfError(callingOrder);
        }
        return null;
    }
}
