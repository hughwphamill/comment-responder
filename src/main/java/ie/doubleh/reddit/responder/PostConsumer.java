package ie.doubleh.reddit.responder;

import net.dean.jraw.models.Submission;
import net.dean.jraw.paginators.RedditIterable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

public class PostConsumer implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(PostConsumer.class);

    private final RedditIterable<Submission> subredditPaginator;
    private final String searchTerm;
    private final Integer pageBufferSize;
    private final Set<String> processedSubmissions;
    private final List<Submission> submissionQueue;

    public PostConsumer(RedditIterable<Submission> subredditPaginator, String searchTerm, Integer pageBufferSize,
            Set<String> processedSubmissions, List<Submission> submissionQueue) {
        this.subredditPaginator = subredditPaginator;
        this.searchTerm = searchTerm;
        this.pageBufferSize = pageBufferSize;
        this.processedSubmissions = processedSubmissions;
        this.submissionQueue = submissionQueue;
    }

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
