package ie.doubleh.reddit.responder;

import net.dean.jraw.models.Submission;
import net.dean.jraw.paginators.RedditIterable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

@Component
public class PostConsumer implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(PostConsumer.class);

    private final RedditIterable<Submission> subredditPaginator;
    private final String searchTerm;
    private final Integer pageBufferSize;
    private final ProcessedSubmissionCache processedSubmissions;
    private final List<Submission> submissionQueue;

    @Autowired
    public PostConsumer(RedditIterable<Submission> subredditPaginator, String searchTerm, Integer pageBufferSize,
            ProcessedSubmissionCache processedSubmissions, List<Submission> submissionQueue) {
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
                .filter(s -> s.getTitle().toLowerCase().contains(searchTerm.toLowerCase()))
                .peek(s -> logger.info("Match: {} | {}", s.getTitle(), s.getPermalink()))
                .forEach(s -> submissionQueue.add(s));
    }

}
