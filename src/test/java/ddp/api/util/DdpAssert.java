package ddp.api.util;

import org.junit.Assert;

/**
 * Created by peter on 13/05/15.
 */
public enum DdpAssert {
    ;

    public static void assertTimeLimit(long maxRuntimeInNanos, long runtimeInNanos) {
        Assert.assertTrue("Took too long, " + runtimeInNanos / 1e9 + " > " + maxRuntimeInNanos / 1e9, runtimeInNanos <= maxRuntimeInNanos);
    }
}
