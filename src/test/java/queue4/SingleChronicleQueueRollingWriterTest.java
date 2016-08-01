package queue4;

import net.openhft.chronicle.core.*;
import net.openhft.chronicle.queue.*;
import net.openhft.chronicle.queue.impl.single.*;
import org.junit.*;
import queue4.chronicle.*;
import queue4.externalizableObjects.*;

/**
 * Created by cliveh on 01/08/2016.
 */
public class SingleChronicleQueueRollingWriterTest
{

    String chronicleQueueBase1 = OS.TARGET + "/Chronicle/dataRolling";


    @Test
    public void testRolling() throws Exception
    {
        // Testing works first
        final RollCycles rollCycle = RollCycles.TEST_SECONDLY;
        final SingleChronicleQueueBuilder builder = ChronicleQueueBuilder
                .single(chronicleQueueBase1)
                .rollCycle(rollCycle);
        final SingleChronicleQueue queue = builder.build();
        EventManager toChronicle = ToChronicle.of(EventManager.class, queue);
        toChronicle.getConfig("Test executor");
    }
}
