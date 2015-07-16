package musiverification;

import org.junit.Assert;
import org.junit.Test;

/**
 * Created by peter.lawrey on 16/07/2015.
 */
public class FailingTest {
    @Test
    public void failingTest() {
        Assert.fail("Should fail");
    }
}
