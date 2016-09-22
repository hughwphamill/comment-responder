package ie.doubleh.reddit.responder.lifecycle;

import ie.doubleh.reddit.responder.bot.SubmissionResponder;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class AsyncInitializer {

    private final SubmissionResponder submissionResponder;

    public AsyncInitializer(SubmissionResponder submissionResponder) {
        this.submissionResponder = submissionResponder;
    }

    @PostConstruct
    public void executeAsyncMethods() {
        submissionResponder.run();
    }
}
