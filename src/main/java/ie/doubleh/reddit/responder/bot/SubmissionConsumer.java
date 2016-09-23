package ie.doubleh.reddit.responder.bot;

import com.aol.cyclops.data.async.Queue;
import ie.doubleh.reddit.responder.bot.model.MatchingSubmission;
import net.dean.jraw.ApiException;
import net.dean.jraw.http.NetworkException;
import net.dean.jraw.models.Submission;
import net.dean.jraw.paginators.RedditIterable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.List;
import java.util.Optional;

public class SubmissionConsumer implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(SubmissionConsumer.class);

    private final RedditIterable<Submission> subredditPaginator;
    private final String searchTerm;
    private final Integer pageBufferSize;
    private final ProcessedSubmissionCache processedSubmissions;
    private final Queue<MatchingSubmission> submissionQueue;

    public SubmissionConsumer(RedditIterable<Submission> subredditPaginator,
            String searchTerm, Integer pageBufferSize, ProcessedSubmissionCache processedSubmissions,
            Queue<MatchingSubmission> submissionQueue) {
        this.subredditPaginator = subredditPaginator;
        this.searchTerm = searchTerm;
        this.pageBufferSize = pageBufferSize;
        this.processedSubmissions = processedSubmissions;
        this.submissionQueue = submissionQueue;
    }

    @Scheduled(fixedDelay=30000)
    @Override public void run() {
        try {
            subredditPaginator.reset();
            List<Submission> listing = subredditPaginator.accumulateMerged(pageBufferSize);

            listing.stream()
                    .filter(s -> !processedSubmissions.contains(s.getId()))
                    .peek(s -> processedSubmissions.add(s.getId()))
                    .filter(this::matchingIgnoringCase)
                    .peek(s -> logger.info("Match for '{}': {} | {}", searchTerm, s.getTitle(), s.getPermalink()))
                    .forEach(s -> submissionQueue.add(MatchingSubmission.of(searchTerm, s)));
        } catch (NetworkException e) {
            logger.error("Error retrieving submissions: {}", e.getMessage());
        }
    }

    private boolean matchingIgnoringCase(Submission submission) {
        String lowerCaseSearchTerm = searchTerm.toLowerCase();
        String lowerCaseTitle = submission.getTitle().toLowerCase();
        Optional<String> lowerCaseSelfText
                = submission.isSelfPost() ? Optional.of(submission.getSelftext().toLowerCase()) : Optional.empty();
        return lowerCaseTitle.contains(lowerCaseSearchTerm)
                || lowerCaseSelfText.orElse("").contains(lowerCaseSearchTerm);
    }

}
