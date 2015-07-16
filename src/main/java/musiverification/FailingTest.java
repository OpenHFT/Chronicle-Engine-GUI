package musiverification;

import org.junit.Test;

import static org.junit.Assert.fail;

/**
 * Created by peter.lawrey on 16/07/2015.
 */
public class FailingTest {
    @Test
    public void failingTest() {
        Assert.fail("Should fail");
    }
}
