package ie.doubleh.reddit.responder.bot;

import com.aol.cyclops.data.async.Queue;
import com.aol.cyclops.internal.stream.ReactiveSeqImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import ie.doubleh.reddit.responder.bot.model.MatchingSubmission;
import net.dean.jraw.ApiException;
import net.dean.jraw.managers.AccountManager;
import net.dean.jraw.models.Submission;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class SubmissionResponderTest {

    private SubmissionResponder responder;

    @Mock private Queue<MatchingSubmission> submissionQueue;
    @Mock private AccountManager accountManager;

    private Submission matchingSubmission1;
    private Submission matchingSubmission2;

    private Fixture fixture;

    @Before
    public void setup() throws Exception {
        fixture = new Fixture();

        initializeSubmissions();

        responder = new SubmissionResponder(submissionQueue, fixture.responseMap, accountManager);
    }

    private void initializeSubmissions() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        ClassLoader classLoader = getClass().getClassLoader();
        File matchingSubmissionFile = new File(classLoader.getResource("matchingSubmissionTitle.json").getFile());
        File unmatchingSubmissionFile = new File(classLoader.getResource("unmatchingSubmissionTitle.json").getFile());
        matchingSubmission1 = new Submission(mapper.readTree(matchingSubmissionFile));
        matchingSubmission2 = new Submission(mapper.readTree(unmatchingSubmissionFile));
    }

    @Test
    public void account_manager_should_reply_to_known_search_term() throws Exception {
        // Given
        given(submissionQueue.stream())
                .willReturn(stream(MatchingSubmission.of(fixture.knownSearch1, matchingSubmission1)));

        // When
        responder.run();

        // Then
        verify(accountManager).reply(matchingSubmission1, fixture.response1);
    }

    @Test
    public void account_manager_should_not_reply_to_unknown_search_term() throws Exception {
        // Given
        given(submissionQueue.stream())
                .willReturn(stream(MatchingSubmission.of(fixture.unknownSearch, matchingSubmission2)));

        // When
        responder.run();

        // Then
        verify(accountManager, never()).reply(any(Submission.class), anyString());
    }

    @Test
    public void responder_should_continue_on_exception() throws Exception {
        // Given
        given(submissionQueue.stream())
                .willReturn(stream(
                        MatchingSubmission.of(fixture.knownSearch1, matchingSubmission1),
                        MatchingSubmission.of(fixture.knownSearch2, matchingSubmission2)));
        given(accountManager.reply(matchingSubmission1, fixture.response1)).willThrow(new ApiException("",""));

        // When
        responder.run();

        // Then
        InOrder inOrder = inOrder(accountManager);
        inOrder.verify(accountManager).reply(matchingSubmission1, fixture.response1);
        inOrder.verify(accountManager).reply(matchingSubmission2, fixture.response2);
    }

    private ReactiveSeqImpl<MatchingSubmission> stream(MatchingSubmission... submissions) {
        return new ReactiveSeqImpl<>(Arrays.stream(submissions));
    }

    private class Fixture {
        private final String knownSearch1 = "knownSearch1";
        private final String knownSearch2 = "knownSearch2";
        private final String unknownSearch = "unknownSearch";
        private final String response1 = "response1";
        private final String response2 = "response2";
        private final Map<String, String> responseMap;

        private Fixture() {
            responseMap = new HashMap<>(1);
            responseMap.put(knownSearch1, response1);
            responseMap.put(knownSearch2, response2);
        }
    }
}
