package ie.doubleh.reddit.responder.bot;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Component
public class ProcessedSubmissionCache {
    
    private final Cache<String, String> cache;

    public ProcessedSubmissionCache() {
        cache = CacheBuilder.newBuilder().expireAfterWrite(2, TimeUnit.HOURS).build();
    }

    public void add(String submissionId) {
        cache.put(submissionId, submissionId);
    }

    public boolean contains(String submissionId) {
        return cache.getIfPresent(submissionId) != null;
    }

    @VisibleForTesting
    Set<String> entries() {
        return new HashSet<>(cache.asMap().keySet());
    }
}
