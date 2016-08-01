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
            System.out.print("Calling getConfig");
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

}
