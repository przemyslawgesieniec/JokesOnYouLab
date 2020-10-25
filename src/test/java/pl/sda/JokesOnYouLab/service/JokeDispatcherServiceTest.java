package pl.sda.JokesOnYouLab.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.mockito.Mockito;
import pl.sda.JokesOnYouLab.model.Joke;
import pl.sda.JokesOnYouLab.model.JokeException;
import pl.sda.JokesOnYouLab.service.external.ChucknorrisJokeProvider;
import pl.sda.JokesOnYouLab.service.external.ExternalJokeProvider;
import pl.sda.JokesOnYouLab.service.external.Sv443JokeProvider;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class JokeDispatcherServiceTest {

    private List<ExternalJokeProvider> externalJokeProviders;
    private JokeDispatcherService jokeDispatcherService;

    public JokeDispatcherServiceTest() {
        this.externalJokeProviders = new ArrayList<>();
    }

    @AfterEach
    void tearDown() {
        externalJokeProviders.clear();
    }

    @Test
    void shouldCallAllGetAvailableCategoriesMethodsCallingProvideJokeCategories() throws JokeException {
        //given
        Sv443JokeProvider sv443JokeProvider = Mockito.mock(Sv443JokeProvider.class);
        ChucknorrisJokeProvider chucknorrisJokeProvider = Mockito.mock(ChucknorrisJokeProvider.class);

        externalJokeProviders.add(sv443JokeProvider);
        externalJokeProviders.add(chucknorrisJokeProvider);

        jokeDispatcherService = new JokeDispatcherService(externalJokeProviders, new RandomnessProvider());

        //when
        jokeDispatcherService.provideJokeCategories();

        //then
        Mockito.verify(sv443JokeProvider, Mockito.times(1)).getAvailableCategories();
        Mockito.verify(chucknorrisJokeProvider, Mockito.times(1)).getAvailableCategories();
    }

    @Test
    void shouldProvideCombinedDistinctElementSet() throws JokeException {
        //given
        Sv443JokeProvider sv443JokeProvider = Mockito.mock(Sv443JokeProvider.class);
        ChucknorrisJokeProvider chucknorrisJokeProvider = Mockito.mock(ChucknorrisJokeProvider.class);

        Mockito.when(sv443JokeProvider.getAvailableCategories()).thenReturn(new HashSet<>(Arrays.asList("fashion", "food")));
        Mockito.when(chucknorrisJokeProvider.getAvailableCategories()).thenReturn(new HashSet<>(Arrays.asList("fashion", "political")));
        Mockito.when(chucknorrisJokeProvider.getAvailableCategories()).thenReturn(new HashSet<>(Arrays.asList("fashion", "political")));

        externalJokeProviders.add(sv443JokeProvider);
        externalJokeProviders.add(chucknorrisJokeProvider);

        jokeDispatcherService = new JokeDispatcherService(externalJokeProviders, new RandomnessProvider());

        //when
        final Set<String> set = jokeDispatcherService.provideJokeCategories();
        assertThat(set).size().isEqualTo(3);
        assertThat(set).containsExactlyInAnyOrder("fashion", "food", "political");
    }

    @Test
    void shouldProvideRandomJokeWhenAllServicesAreAvailable() throws JokeException {
        //given
        jokeDispatcherService = new JokeDispatcherService(externalJokeProviders, new RandomnessProvider());
        Sv443JokeProvider sv443JokeProvider = Mockito.mock(Sv443JokeProvider.class);
        ChucknorrisJokeProvider chucknorrisJokeProvider = Mockito.mock(ChucknorrisJokeProvider.class);

        Mockito.when(sv443JokeProvider.getRandomJoke()).thenReturn(Mockito.mock(Joke.class));
        Mockito.when(chucknorrisJokeProvider.getRandomJoke()).thenReturn(Mockito.mock(Joke.class));
        externalJokeProviders.add(sv443JokeProvider);
        externalJokeProviders.add(chucknorrisJokeProvider);

        //when
        jokeDispatcherService.provideRandomJoke();

        //then
        if (Mockito.mockingDetails(sv443JokeProvider).getInvocations().isEmpty()) {
            Mockito.verify(chucknorrisJokeProvider, Mockito.times(1)).getRandomJoke();
            Mockito.verify(sv443JokeProvider, Mockito.times(0)).getRandomJoke();
        } else {
            Mockito.verify(chucknorrisJokeProvider, Mockito.times(0)).getRandomJoke();
            Mockito.verify(sv443JokeProvider, Mockito.times(1)).getRandomJoke();
        }
    }

    @Test
    void shouldProvideRandomJokeWhenNotAllServicesAreAvailable() throws Throwable {
        //given
        Sv443JokeProvider sv443JokeProvider = Mockito.mock(Sv443JokeProvider.class);
        ChucknorrisJokeProvider chucknorrisJokeProvider = Mockito.mock(ChucknorrisJokeProvider.class);
        RandomnessProvider randomnessProviderMock = Mockito.mock(RandomnessProvider.class);

        externalJokeProviders.add(chucknorrisJokeProvider);
        externalJokeProviders.add(sv443JokeProvider);

        List<Integer> list = new ArrayList<>();
        list.add(0);
        list.add(1);

        Mockito.when(sv443JokeProvider.getRandomJoke()).thenReturn(Mockito.mock(Joke.class));
        Mockito.when(chucknorrisJokeProvider.getRandomJoke()).thenThrow(new JokeException("Error"));
        Mockito.when(randomnessProviderMock.determineCallingOrder(Mockito.anyInt()))
                .thenReturn(list);


        jokeDispatcherService = new JokeDispatcherService(externalJokeProviders, randomnessProviderMock);


        //when
        Executable executable = () -> jokeDispatcherService.provideRandomJoke();
        executable.execute();

        //then
        Mockito.verify(chucknorrisJokeProvider, Mockito.times(1)).getRandomJoke();
        Mockito.verify(sv443JokeProvider, Mockito.times(1)).getRandomJoke();
        assertDoesNotThrow(executable);
    }


}
