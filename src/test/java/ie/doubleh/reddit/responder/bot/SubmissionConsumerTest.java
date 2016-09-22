package ie.doubleh.reddit.responder.bot;

import com.aol.cyclops.data.async.Queue;
import com.fasterxml.jackson.databind.ObjectMapper;
import ie.doubleh.reddit.responder.bot.model.MatchingSubmission;
import net.dean.jraw.models.Submission;
import net.dean.jraw.paginators.RedditIterable;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class SubmissionConsumerTest {

    private SubmissionConsumer submissionConsumer;

    @Mock private RedditIterable<Submission> subredditPaginator;
    @Mock private Queue<MatchingSubmission> submissionQueue;

    private Submission matchingSubmission;
    private Submission unmatchingSubmission;
    private Submission matchingSelfSubmission;
    private Submission unmatchingSelfSubmission;

    private Integer pageBufferSize;
    private ProcessedSubmissionCache processedSubmissions;

    private Fixture fixture;

    @Before
    public void setUp() throws Exception {
        fixture = new Fixture();

        pageBufferSize = fixture.pageBufferSize;
        processedSubmissions = new ProcessedSubmissionCache(3, TimeUnit.HOURS);

        initializeSubmissions();

        submissionConsumer = new SubmissionConsumer(subredditPaginator, fixture.searchTerm, pageBufferSize, processedSubmissions, submissionQueue);
    }

    private void initializeSubmissions() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        ClassLoader classLoader = getClass().getClassLoader();
        File matchingSubmissionFile = new File(classLoader.getResource("matchingSubmissionTitle.json").getFile());
        File unmatchingSubmissionFile = new File(classLoader.getResource("unmatchingSubmissionTitle.json").getFile());
        File matchingSelfSubmissionFile = new File(classLoader.getResource("matchingSelfSubmission.json").getFile());
        File unmatchingSelfSubmissionFile = new File(classLoader.getResource("unmatchingSelfSubmission.json").getFile());
        matchingSubmission = new Submission(mapper.readTree(matchingSubmissionFile));
        unmatchingSubmission = new Submission(mapper.readTree(unmatchingSubmissionFile));
        matchingSelfSubmission = new Submission(mapper.readTree(matchingSelfSubmissionFile));
        unmatchingSelfSubmission = new Submission(mapper.readTree(unmatchingSelfSubmissionFile));
    }

    @Test
    public void paginator_should_be_reset() throws Exception {
        // Given
        given(subredditPaginator.accumulateMerged(anyInt())).willReturn(Collections.emptyList());

        // When
        submissionConsumer.run();


        // Then
        verify (subredditPaginator).reset();
    }

    @Test
    public void paginator_should_use_buffer_size_input() throws Exception {
        // Given
        given(subredditPaginator.accumulateMerged(anyInt())).willReturn(Collections.emptyList());

        // When
        submissionConsumer.run();


        // Then
        verify (subredditPaginator).accumulateMerged(fixture.pageBufferSize);
    }

    @Test
    public void all_submissions_should_be_added_to_processed() throws Exception {
        // Given
        given(subredditPaginator.accumulateMerged(anyInt())).willReturn(
                Arrays.asList(matchingSubmission, unmatchingSubmission));

        // When
        submissionConsumer.run();

        // Then
        assertThat(processedSubmissions.entries()).contains(matchingSubmission.getId(), unmatchingSubmission.getId());
    }

    @Test
    public void matching_submission_should_be_added_to_queue() throws Exception {
        // Given
        given(subredditPaginator.accumulateMerged(anyInt())).willReturn(
                Arrays.asList(matchingSubmission, unmatchingSubmission));

        // When
        submissionConsumer.run();

        // Then
        verify(submissionQueue).add(MatchingSubmission.of(fixture.searchTerm, matchingSubmission));
    }

    @Test
    public void matching_submission_should_be_added_to_regardless_of_case() throws Exception {
        // Given
        submissionConsumer = new SubmissionConsumer(subredditPaginator, fixture.searchTerm.toUpperCase(), pageBufferSize,
                processedSubmissions, submissionQueue);
        given(subredditPaginator.accumulateMerged(anyInt())).willReturn(
                Arrays.asList(matchingSubmission, unmatchingSubmission));

        // When
        submissionConsumer.run();

        // Then
        verify(submissionQueue).add(MatchingSubmission.of(fixture.searchTerm.toUpperCase(), matchingSubmission));
    }

    @Test
    public void unmatching_submission_should_not_be_added_to_queue() throws Exception {
        // Given
        given(subredditPaginator.accumulateMerged(anyInt())).willReturn(
                Arrays.asList(matchingSubmission, unmatchingSubmission));

        // When
        submissionConsumer.run();

        // Then
        verify(submissionQueue, never()).add(MatchingSubmission.of(fixture.searchTerm, unmatchingSubmission));
    }

    @Test
    public void matching_self_submission_should_be_added_to_queue() throws Exception {
        // Given
        given(subredditPaginator.accumulateMerged(anyInt())).willReturn(
                Arrays.asList(matchingSelfSubmission, unmatchingSelfSubmission));

        // When
        submissionConsumer.run();

        // Then
        verify(submissionQueue).add(MatchingSubmission.of(fixture.searchTerm, matchingSelfSubmission));
    }

    @Test
    public void unmatching_self_submission_should_not_be_added_to_queue() throws Exception {
        // Given
        given(subredditPaginator.accumulateMerged(anyInt())).willReturn(
                Arrays.asList(matchingSelfSubmission, unmatchingSelfSubmission));

        // When
        submissionConsumer.run();

        // Then
        verify(submissionQueue, never()).add(MatchingSubmission.of(fixture.searchTerm, unmatchingSelfSubmission));
    }

    private class Fixture {
        private final int pageBufferSize = 4;
        private final String searchTerm = "matching";
        private final String unmatchingTitle = "Nothing in particular";
        private final String matchingTitle = "This title is " + searchTerm + " the input";
    }
}
