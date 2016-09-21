package ie.doubleh.reddit.responder;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.dean.jraw.models.Submission;
import net.dean.jraw.paginators.RedditIterable;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class PostConsumerTest {

    private PostConsumer postConsumer;

    @Mock private RedditIterable<Submission> subredditPaginator;

    private Submission matchingSubmission;
    private Submission unmatchingSubmission;

    private Integer pageBufferSize;
    private ProcessedSubmissionCache processedSubmissions;
    private List<Submission> submissionQueue;

    private Fixture fixture;

    @Before
    public void setUp() throws Exception {
        fixture = new Fixture();

        pageBufferSize = fixture.pageBufferSize;
        processedSubmissions = new ProcessedSubmissionCache();
        submissionQueue = new ArrayList<>();

        initializeSubmissions();

        postConsumer = new PostConsumer(subredditPaginator, fixture.searchTerm, pageBufferSize, processedSubmissions, submissionQueue);
    }

    private void initializeSubmissions() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        ClassLoader classLoader = getClass().getClassLoader();
        File matchingSubmissionFile = new File(classLoader.getResource("matchingSubmissionTitle.json").getFile());
        File unmatchingSubmissionFile = new File(classLoader.getResource("unmatchingSubmissionTitle.json").getFile());
        matchingSubmission = new Submission(mapper.readTree(matchingSubmissionFile));
        unmatchingSubmission = new Submission(mapper.readTree(unmatchingSubmissionFile));
    }

    @Test
    public void paginator_should_be_reset() throws Exception {
        // Given
        given(subredditPaginator.accumulateMerged(anyInt())).willReturn(Collections.emptyList());

        // When
        postConsumer.run();


        // Then
        verify (subredditPaginator).reset();
    }

    @Test
    public void paginator_should_use_buffer_size_input() throws Exception {
        // Given
        given(subredditPaginator.accumulateMerged(anyInt())).willReturn(Collections.emptyList());

        // When
        postConsumer.run();


        // Then
        verify (subredditPaginator).accumulateMerged(fixture.pageBufferSize);
    }

    @Test
    public void all_submissions_should_be_added_to_processed() throws Exception {
        // Given
        given(subredditPaginator.accumulateMerged(anyInt())).willReturn(
                Arrays.asList(matchingSubmission, unmatchingSubmission));

        // When
        postConsumer.run();

        // Then
        assertThat(processedSubmissions.entries()).contains(matchingSubmission.getId(), unmatchingSubmission.getId());
    }

    @Test
    public void matching_submission_should_be_added_to_queue() throws Exception {
        // Given
        given(subredditPaginator.accumulateMerged(anyInt())).willReturn(
                Arrays.asList(matchingSubmission, unmatchingSubmission));

        // When
        postConsumer.run();

        // Then
        assertThat(submissionQueue).extracting(s -> s.getTitle()).contains(matchingSubmission.getTitle());
    }

    @Test
    public void matching_submission_should_be_added_to_regardless_of_case() throws Exception {
        // Given
        postConsumer = new PostConsumer(subredditPaginator, fixture.searchTerm.toUpperCase(), pageBufferSize,
                processedSubmissions, submissionQueue);
        given(subredditPaginator.accumulateMerged(anyInt())).willReturn(
                Arrays.asList(matchingSubmission, unmatchingSubmission));

        // When
        postConsumer.run();

        // Then
        assertThat(submissionQueue).extracting(s -> s.getTitle()).contains(matchingSubmission.getTitle());
    }

    @Test
    public void unmatching_submission_should_not_be_added_to_queue() throws Exception {
        // Given
        given(subredditPaginator.accumulateMerged(anyInt())).willReturn(
                Arrays.asList(matchingSubmission, unmatchingSubmission));

        // When
        postConsumer.run();

        // Then
        assertThat(submissionQueue).extracting(s -> s.getTitle()).doesNotContain(unmatchingSubmission.getTitle());
    }

    private class Fixture {
        private final int pageBufferSize = 4;
        private final String searchTerm = "matching";
        private final String unmatchingTitle = "Nothing in particular";
        private final String matchingTitle = "This title is " + searchTerm + " the input";
    }
}
