package ie.doubleh.reddit.responder.bot.model;

import net.dean.jraw.models.Submission;

import java.util.Objects;

public class MatchingSubmission {

    private final String searchTerm;
    private final Submission submission;

    private MatchingSubmission(String searchTerm, Submission submission) {
        this.searchTerm = searchTerm;
        this.submission = submission;
    }

    public static MatchingSubmission of(String searchTerm, Submission submission) {
        return new MatchingSubmission(searchTerm, submission);
    }

    public String getSearchTerm() {
        return searchTerm;
    }

    public Submission getSubmission() {
        return submission;
    }

    @Override public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof MatchingSubmission))
            return false;
        MatchingSubmission that = (MatchingSubmission) o;
        return Objects.equals(searchTerm, that.searchTerm) &&
                Objects.equals(submission, that.submission);
    }

    @Override public int hashCode() {
        return Objects.hash(searchTerm, submission);
    }
}
