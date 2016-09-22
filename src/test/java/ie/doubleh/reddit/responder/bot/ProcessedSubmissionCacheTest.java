package ie.doubleh.reddit.responder.bot;

import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

public class ProcessedSubmissionCacheTest {

    private ProcessedSubmissionCache cache;

    @Before
    public void setup() throws Exception {
        cache = new ProcessedSubmissionCache(3, TimeUnit.HOURS);
    }

    @Test
    public void putting_value_in_cache_should_be_retrievable() throws Exception {
        // Given
        String expected = "expected";

        // When
        cache.add(expected);
        boolean isPresent = cache.contains(expected);

        // Then
        assertThat(isPresent).isTrue();
    }

    @Test
    public void value_not_in_cache_should_not_be_retrievable() throws Exception {
        // Given
        String expected = "expected";

        // When
        boolean isPresent = cache.contains(expected);

        // Then
        assertThat(isPresent).isFalse();
    }

    @Test
    public void value_should_be_evicted_according_to_cache_policy() throws Exception {
        // Given
        String expected = "expected";
        cache =  new ProcessedSubmissionCache(2, TimeUnit.MILLISECONDS);

        // When
        cache.add(expected);
        Thread.sleep(10);
        boolean isPresent = cache.contains(expected);

        // Then
        assertThat(isPresent).isFalse();
    }
}
