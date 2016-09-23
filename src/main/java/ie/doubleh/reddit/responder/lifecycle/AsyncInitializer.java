package ie.doubleh.reddit.responder.lifecycle;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.HashSet;
import java.util.Set;

@Component
public class AsyncInitializer {

    private final Set<Runnable> runnables;

    @Autowired
    public AsyncInitializer(@Qualifier("submissionResponder") Runnable submissionResponder,
            @Qualifier("periodicAuthenticator") Runnable periodicAuthenticator) {
        runnables = new HashSet<>();
        runnables.add(submissionResponder);
        runnables.add(periodicAuthenticator);
    }

    @PostConstruct
    public void executeAsyncMethods() {
        runnables.parallelStream().forEach(Runnable::run);
    }
}
