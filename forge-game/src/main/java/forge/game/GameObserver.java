package forge.game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;

/**
 * Extension point for code outside Forge that wants to watch games: recorders, statistics,
 * replay tools. Every implementation registered in
 * {@code META-INF/services/forge.game.GameObserver} on the classpath is told about each game
 * Forge creates, whatever started it (desktop or mobile GUI, network play, {@code sim} mode).
 * A typical observer calls {@link Game#subscribeToEvents(Object)}.
 */
public interface GameObserver {
    /** Called once per game, after it is created and before it starts. */
    void gameCreated(Game game);

    /** Notifies every registered observer; one that fails is reported and skipped. */
    static void notifyCreated(Game game) {
        for (GameObserver observer : Registry.OBSERVERS) {
            try {
                observer.gameCreated(game);
            } catch (RuntimeException e) {
                System.err.println("GameObserver " + observer.getClass().getName() + " failed: " + e);
            }
        }
    }

    /** Loaded once, lazily: the classpath doesn't change while Forge runs. */
    final class Registry {
        private static final List<GameObserver> OBSERVERS = load();

        private Registry() {}

        private static List<GameObserver> load() {
            List<GameObserver> found = new ArrayList<>();
            try {
                for (GameObserver observer : ServiceLoader.load(GameObserver.class)) {
                    found.add(observer);
                }
            } catch (ServiceConfigurationError e) {
                System.err.println("Could not load a GameObserver: " + e.getMessage());
            }
            return Collections.unmodifiableList(found);
        }
    }
}
