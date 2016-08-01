package queue4;

import java.io.*;

import net.openhft.chronicle.core.*;
import net.openhft.chronicle.core.threads.*;
import net.openhft.chronicle.queue.*;
import net.openhft.chronicle.queue.impl.single.*;
import org.junit.*;
import queue4.chronicle.*;
import queue4.externalizableObjects.*;

/**
 * Created by cliveh on 01/08/2016.
 */
public class SingleChronicleQueueRollingTest
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
        final ExcerptTailer tailer = queue.createTailer();
        TestGetConfigEventManager testEventManager = new TestGetConfigEventManager();
        FromChronicle<TestGetConfigEventManager> fromChronicle = FromChronicle.of(testEventManager, tailer);
        // The test does not fail when run in the same process.
        // Only seems to fail to read 2nd message (i.e. once rolled if writer in separate process)
//        Thread writingThread = new Thread(new WritingTask());
//        writingThread.start();
        // Now have something that writes
        Thread.sleep(5_000);
        System.out.println("Reading first message");
        boolean isRead = fromChronicle.readOne();
        Assert.assertTrue(isRead);
        Thread.sleep(5_000);
        System.out.println("Reading second message");
        isRead = fromChronicle.readOne();
        Assert.assertTrue(isRead);
    }

    public class TestGetConfigEventManager implements EventManager
    {

        @Override
        public String getExecutor()
        {
            return "Test";
        }


        @Override
        public void getConfig(String executor)
        {
            System.out.println("Calling getConfig");
        }


        @Override
        public void setConfig(String executor)
        {

        }


        @Override
        public void onConfigAdd(String executor, ConfigSetting addedConfigSetting)
        {

        }


        @Override
        public void onConfigUpdate(String executor, ConfigSetting updateConfigSetting)
        {

        }


        @Override
        public void onConfigRemove(String executor, ConfigSetting removedConfigSetting)
        {

        }


        @Override
        public void getMarketData(String executor)
        {

        }


        @Override
        public void setMarketData(String executor)
        {

        }


        @Override
        public void onMarketDataUpdate(String producer, MarketDataSupplier supplier, MarketDataSource source, MarketDataType type, String id, byte[] marketDataUpdates, boolean isRetransmit) throws Exception
        {

        }


        @Override
        public void process(String executor)
        {

        }


        @Override
        public boolean hasChanged()
        {
            return false;
        }


        @Override
        public boolean isInitialized()
        {
            return false;
        }
    }


    public class WritingTask implements Runnable
    {
        @Override
        public void run()
        {
            final RollCycles rollCycle = RollCycles.TEST_SECONDLY;
            final SingleChronicleQueueBuilder builder = ChronicleQueueBuilder
                    .single(chronicleQueueBase1)
                    .rollCycle(rollCycle);
            final SingleChronicleQueue queue = builder.build();
//        final ExcerptTailer tailer = queue.createTailer();
//        TestGetConfigEventManager testEventManager = new TestGetConfigEventManager();
//        FromChronicle<TestGetConfigEventManager> fromChronicle = FromChronicle.of(testEventManager, tailer);
            // Now have something that writes
            EventManager toChronicle = null;
            try
            {
                toChronicle = ToChronicle.of(EventManager.class, queue);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }

            // Write first message immediately.
            System.out.println("Writing first message");
            toChronicle.getConfig("Test executor");


            // Wait 2 seconds for 2nd message to ensure file rolls

            try
            {
                Thread.sleep(7_000);
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }

            System.out.println("Writing second message");
            toChronicle.getConfig("Test executor");
        }
    }
}
