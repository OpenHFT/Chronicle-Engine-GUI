package adept.publisher;

import java.util.*;
import java.util.concurrent.*;
import net.openhft.chronicle.engine.server.*;
import net.openhft.chronicle.engine.tree.*;
import net.openhft.chronicle.wire.*;
import org.apache.logging.log4j.*;

import adept.common.*;
import adept.common.marketdata.*;
import adept.datamanager.publisher.*;

/**
 * This class contains market data and pricer output data in Chronicle map for GUI
 * to subscribe to.
 * <p>
 * NB This might well move to Bootstrap Manager but required somewhere to hook up interactions
 * Currently with random data.
 */
public class SystemPublisher
{
    private static final Logger _logger = LogManager.getLogger(SystemPublisher.class);

    /**
     * Main method
     *
     * @param args
     */
    public static void main(String... args)
    {
        _logger.info("Starting systems publisher...");

        try
        {
            final VanillaAssetTree assetTree = new VanillaAssetTree().forServer(false);
            ServerEndpoint serverEndpoint = new ServerEndpoint("*:" + 7799, assetTree, WireType.BINARY);
            Map<String, Double> marketDataMap = assetTree.acquireMap("/adept/marketdata/realtime", String.class, Double.class);
            marketDataMap.put("1", 7.75);
            marketDataMap.put("2", 8.63);
            marketDataMap.put("3", 9.0);
            marketDataMap.put("4", 9.375);
            marketDataMap.put("5", 10.125);
            marketDataMap.put("6", 10.875);
            marketDataMap.put("7", 12.0);
            marketDataMap.put("8", 13.0);
            marketDataMap.put("9", 13.5);
            marketDataMap.put("10", 13.5);
            marketDataMap.put("11", 13.5);
            marketDataMap.put("12", 13.375);
            marketDataMap.put("13", 13.125);
            marketDataMap.put("14", 12.75);
            marketDataMap.put("15", 12.375);
            marketDataMap.put("16", 12.375);
            marketDataMap.put("17", 12.75);
            marketDataMap.put("18", 13.0);
            marketDataMap.put("19", 13.125);

            String[] marketDataKeys = marketDataMap.keySet().toArray(new String[marketDataMap.size()]);
            try
            {
                for (; ; )
                {

                    for (int i = 1; i < marketDataKeys.length - 1; i++)
                    {
                        String marketDataKey = marketDataKeys[i];
                        Double mktValue = marketDataMap.get(marketDataKey);
                        // Just add 1;
                        Double newValue = mktValue + 1.00;
                        marketDataMap.put(marketDataKey, newValue);
                        System.out.println("Key:" + marketDataKey + ", New Value: " + newValue);
                    }
                    Thread.sleep(1000);
                }
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }

        }
        catch (Exception e)
        {
            _logger.error("An exception occurred: ", e);
        }
    }
}
