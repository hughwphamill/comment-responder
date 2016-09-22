package ie.doubleh.reddit.responder.bot;

import com.aol.cyclops.data.async.Queue;
import ie.doubleh.reddit.responder.bot.model.MatchingSubmission;
import net.dean.jraw.models.Submission;
import net.dean.jraw.paginators.RedditIterable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class SubmissionConsumer implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(SubmissionConsumer.class);

    private final RedditIterable<Submission> subredditPaginator;
    private final String searchTerm;
    private final Integer pageBufferSize;
    private final ProcessedSubmissionCache processedSubmissions;
    private final Queue<MatchingSubmission> submissionQueue;

    @Autowired
    public SubmissionConsumer(RedditIterable<Submission> subredditPaginator, String searchTerm, Integer pageBufferSize,
            ProcessedSubmissionCache processedSubmissions, Queue<MatchingSubmission> submissionQueue) {
        this.subredditPaginator = subredditPaginator;
        this.searchTerm = searchTerm;
        this.pageBufferSize = pageBufferSize;
        this.processedSubmissions = processedSubmissions;
        this.submissionQueue = submissionQueue;
    }

    @Scheduled(fixedDelay=5000)
    @Override public void run() {
        subredditPaginator.reset();

        List<Submission> listing = subredditPaginator.accumulateMerged(pageBufferSize);

        listing.stream()
                .filter(s -> !processedSubmissions.contains(s.getId()))
                .peek(s -> processedSubmissions.add(s.getId()))
                .filter(this::matchingIgnoringCase)
                .peek(s -> logger.info("Match: {} | {}", s.getTitle(), s.getPermalink()))
                .forEach(s -> submissionQueue.add(MatchingSubmission.of(searchTerm, s)));
    }

    private boolean matchingIgnoringCase(Submission submission) {
        String lowerCaseSearchTerm = searchTerm.toLowerCase();
        String lowerCaseTitle = submission.getTitle().toLowerCase();
        Optional<String> lowerCaseSelfText
                = submission.isSelfPost() ? Optional.of(submission.getSelftext()) : Optional.empty();
        return lowerCaseTitle.contains(lowerCaseSearchTerm)
                || lowerCaseSelfText.orElse("").contains(lowerCaseSearchTerm);
    }

}
