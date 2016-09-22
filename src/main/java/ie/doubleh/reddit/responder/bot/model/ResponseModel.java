package ie.doubleh.reddit.responder.bot.model;

import java.util.List;
import java.util.Map;

public class ResponseModel {

    private List<String> subreddits;
    private Map<String, String> submissionResponses;

    public Map<String, String> getSubmissionResponses() {
        return submissionResponses;
    }

    public void setSubmissionResponses(Map<String, String> submissionResponses) {
        this.submissionResponses = submissionResponses;
    }

    public List<String> getSubreddits() {
        return subreddits;
    }

    public void setSubreddits(List<String> subreddits) {
        this.subreddits = subreddits;
    }
}
