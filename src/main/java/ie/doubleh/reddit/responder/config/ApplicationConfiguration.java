package ie.doubleh.reddit.responder.config;

import com.aol.cyclops.data.async.Queue;
import com.aol.cyclops.data.async.QueueFactories;
import com.fasterxml.jackson.databind.ObjectMapper;
import ie.doubleh.reddit.responder.bot.ProcessedSubmissionCache;
import ie.doubleh.reddit.responder.bot.SubmissionConsumer;
import ie.doubleh.reddit.responder.bot.model.ApplicationCredentials;
import ie.doubleh.reddit.responder.bot.model.ApplicationUserAgent;
import ie.doubleh.reddit.responder.bot.model.MatchingSubmission;
import ie.doubleh.reddit.responder.bot.model.ResponseModel;
import net.dean.jraw.RedditClient;
import net.dean.jraw.http.UserAgent;
import net.dean.jraw.http.oauth.Credentials;
import net.dean.jraw.http.oauth.OAuthData;
import net.dean.jraw.http.oauth.OAuthException;
import net.dean.jraw.managers.AccountManager;
import net.dean.jraw.models.LoggedInAccount;
import net.dean.jraw.models.Submission;
import net.dean.jraw.paginators.Paginator;
import net.dean.jraw.paginators.Sorting;
import net.dean.jraw.paginators.SubredditPaginator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.core.io.FileSystemResource;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.io.File;
import java.io.IOException;
import java.util.List;

@EnableAsync
@EnableScheduling
@Configuration
public class ApplicationConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(ApplicationConfiguration.class);

    @Value("${responsemodel.file:responses.json}")
    private String responseModelFileName;
    @Value("${credentials.file:credentials.json}")
    private String credentialFileName;
    @Value("${useragent.file:useragent.json}")
    private String useragentFileName;
    @Value("${pagebuffer.size:3}")
    private Integer pageBufferSize;

    @Autowired
    private ProcessedSubmissionCache processedSubmissionCache;

    @Bean
    public Queue<MatchingSubmission> submissionQueue() {
        return QueueFactories.<MatchingSubmission>unboundedQueue().build();
    }

    @Bean
    public ResponseModel responseModel() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        FileSystemResource responseModelResource = new FileSystemResource(responseModelFileName);
        return mapper.readValue(responseModelResource.getFile(), ResponseModel.class);
    }

    @Bean
    public ApplicationCredentials applicationCredentials() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        FileSystemResource credentialResource = new FileSystemResource(credentialFileName);
        return mapper.readValue(credentialResource.getFile(), ApplicationCredentials.class);
    }

    @Bean
    public ApplicationUserAgent applicationUserAgent() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        FileSystemResource useragentResource = new FileSystemResource(useragentFileName);
        return mapper.readValue(useragentResource.getFile(), ApplicationUserAgent.class);
    }

    @Bean
    public RedditClient redditClient() throws IOException, OAuthException {
        ApplicationCredentials appCredentials = applicationCredentials();
        Credentials credentials =
                Credentials.script(appCredentials.getUsername(),
                        appCredentials.getPassword(),
                        appCredentials.getApplicationId(),
                        appCredentials.getApplicationSecret());

        ApplicationUserAgent appUa = applicationUserAgent();

        UserAgent ua =
                UserAgent.of(appUa.getPlatform(), appUa.getAppId(), appUa.getVersion(), appUa.getRedditUsername());

        RedditClient reddit = new RedditClient(ua);

        OAuthData auth = reddit.getOAuthHelper().easyAuth(credentials);
        reddit.authenticate(auth);
        LoggedInAccount response = reddit.me();
        logger.info("Connected to Reddit as {}, {}", response.getId(), response.getFullName());
        return reddit;
    }

    @Bean
    public AccountManager accountManager() throws IOException, OAuthException {
        return new AccountManager(redditClient());
    }

    @Bean
    @Scope("prototype")
    public SubmissionConsumer submissionConsumer(String searchTerm) throws IOException, OAuthException {
         return new SubmissionConsumer(subredditPaginator(), searchTerm,
                 pageBufferSize, processedSubmissionCache, submissionQueue());
    }

    private SubredditPaginator subredditPaginator() throws IOException, OAuthException {
        List<String> subreddits = responseModel().getSubreddits();
        if (!subreddits.isEmpty()) {
            String primarySubreddit = subreddits.get(0);
            String[] otherSubreddits = subreddits.stream().skip(1).toArray(String[]::new);

            SubredditPaginator paginator = new SubredditPaginator(redditClient(), primarySubreddit, otherSubreddits);
            paginator.setLimit(Paginator.RECOMMENDED_MAX_LIMIT);
            paginator.setSorting(Sorting.NEW);
            return paginator;
        } else {
            return new SubredditPaginator(redditClient());
        }
    }
}
