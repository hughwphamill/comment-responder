package ie.doubleh.reddit.responder.bot;

import com.aol.cyclops.data.async.Queue;
import ie.doubleh.reddit.responder.bot.model.MatchingSubmission;
import ie.doubleh.reddit.responder.bot.model.ResponseModel;
import net.dean.jraw.ApiException;
import net.dean.jraw.managers.AccountManager;
import net.dean.jraw.models.Submission;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class SubmissionResponder implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(SubmissionResponder.class);

    private final Queue<MatchingSubmission> submissionQueue;
    private final Map<String, String> responseMap;
    private final AccountManager accountManager;

    @Autowired
    public SubmissionResponder(Queue<MatchingSubmission> submissionQueue, ResponseModel responseModel,
            AccountManager accountManager) {
        this.submissionQueue = submissionQueue;
        this.responseMap = responseModel.getSubmissionResponses();
        this.accountManager = accountManager;
    }

    @Async
    @Override public void run() {
        submissionQueue.stream().forEach(this::reply);
    }

    private void reply(MatchingSubmission matchingSubmission) {
        final String searchTerm = matchingSubmission.getSearchTerm();
        final Submission submission = matchingSubmission.getSubmission();
        if (responseMap.containsKey(searchTerm)) {
            try {
                accountManager.reply(submission, responseMap.get(searchTerm));
            } catch (ApiException e) {
                logger.error("Could not reply to {} : {}", submission.getPermalink(), e.getMessage());
            }
        }
    }
}
