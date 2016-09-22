package ie.doubleh.reddit.responder.config;

import com.aol.cyclops.data.async.Queue;
import com.aol.cyclops.data.async.QueueFactories;
import ie.doubleh.reddit.responder.bot.model.MatchingSubmission;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableAsync
@EnableScheduling
@Configuration
public class ApplicationConfiguration {

    @Bean
    public Queue<MatchingSubmission> submissionQueue() {
        return QueueFactories.<MatchingSubmission>unboundedQueue().build();
    }
}
