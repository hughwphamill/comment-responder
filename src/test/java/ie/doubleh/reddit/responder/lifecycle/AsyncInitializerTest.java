package ie.doubleh.reddit.responder.lifecycle;

import ie.doubleh.reddit.responder.bot.SubmissionResponder;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class AsyncInitializerTest {

    private AsyncInitializer asyncInitializer;

    @Mock private SubmissionResponder submissionResponder;
    @Mock private PeriodicAuthenticator authenticator;

    @Before
    public void setup() {
         asyncInitializer = new AsyncInitializer(submissionResponder, authenticator);
    }

    @Test
    public void should_start_submission_responder() throws Exception {
        // When
        asyncInitializer.executeAsyncMethods();

        // Then
        verify(submissionResponder).run();
    }

    @Test
    public void should_start_authenticator() throws Exception {
        // When
        asyncInitializer.executeAsyncMethods();

        // Then
        verify(authenticator).run();
    }
}
