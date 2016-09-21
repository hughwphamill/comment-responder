package ie.doubleh.reddit.responder;

import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;

public class ProcessedSubmissionCacheTest {

    private ProcessedSubmissionCache cache;

    @Before
    public void setup() throws Exception {
        cache = new ProcessedSubmissionCache();
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

}
