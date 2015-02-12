package jp.mufg.api.util;

import net.openhft.lang.values.LongValue;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertEquals;

public class CountersMapTest {

    @Test
    public void testAcquireCounter() throws Exception {
        String file = "countersMap-" + System.nanoTime();
        new File(file).deleteOnExit();
        {
            CountersMap cm = new CountersMap(file);
            LongValue one = cm.acquireCounter("one");
            one.setOrderedValue(1);
            LongValue two = cm.acquireCounter("two");
            two.setOrderedValue(10);
            cm.close();
        }
        {
            CountersMap cm = new CountersMap(file);
            LongValue one = cm.acquireCounter("one");
            assertEquals(2, one.addAtomicValue(1));
            LongValue two = cm.acquireCounter("two");
            assertEquals(20, two.addAtomicValue(10));
            cm.close();
        }
    }
}