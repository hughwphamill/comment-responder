package ie.doubleh.reddit.responder.lifecycle;

import ie.doubleh.reddit.responder.bot.model.ApplicationCredentials;
import net.dean.jraw.RedditClient;
import net.dean.jraw.http.oauth.Credentials;
import net.dean.jraw.http.oauth.OAuthData;
import net.dean.jraw.http.oauth.OAuthException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class PeriodicAuthenticator implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(PeriodicAuthenticator.class);

    private static final long PERIOD = 55 * 60 * 1000;

    private final RedditClient reddit;
    private final ApplicationCredentials appCredentials;

    @Autowired
    public PeriodicAuthenticator(RedditClient reddit, ApplicationCredentials appCredentials) {
        this.reddit = reddit;
        this.appCredentials = appCredentials;
    }

    @Scheduled(fixedRate = PERIOD, initialDelay = PERIOD)
    @Override public void run() {
        Credentials credentials =
                Credentials.script(appCredentials.getUsername(),
                        appCredentials.getPassword(),
                        appCredentials.getApplicationId(),
                        appCredentials.getApplicationSecret());

        try {
            OAuthData auth = reddit.getOAuthHelper().easyAuth(credentials);
            reddit.authenticate(auth);
        } catch (OAuthException e) {
            logger.error("Error Authenticating Client", e.getStackTrace());
        }
    }
}
