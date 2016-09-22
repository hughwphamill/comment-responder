package ie.doubleh.reddit.responder.lifecycle;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class AsyncInitializer {

    private final Runnable submissionResponder;

    @Autowired
    public AsyncInitializer(@Qualifier("submissionResponder") Runnable submissionResponder) {
        this.submissionResponder = submissionResponder;
    }

    @PostConstruct
    public void executeAsyncMethods() {
        submissionResponder.run();
    }
}
