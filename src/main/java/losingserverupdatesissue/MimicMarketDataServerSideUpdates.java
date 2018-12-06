package losingserverupdatesissue;

import net.openhft.chronicle.engine.server.ServerEndpoint;
import net.openhft.chronicle.engine.tree.VanillaAssetTree;

import java.io.BufferedReader;
import java.io.FileReader;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * This class mimics the server side market data updates that occur in Data Manager. It was
 * originally written to highlight an issue whereby updates from server side (1.12.27-SNAPSHOT) were
 * no longer being received after a couple of minutes.
 */
public class MimicMarketDataServerSideUpdates {
    private static VanillaAssetTree _assetTree;
    private static ServerEndpoint _serverEndpoint;

    /**
     * Entry point
     *
     * @param args Single argument with file location should be specified.  The file contains
     *             updates to process.
     */
    public static void main(String[] args) throws Exception {

        String workingDirectory = System.getProperty("user.dir");
        String updatesFile = Paths.get(workingDirectory, "src/main/resources/DataManagerExtraLoggingForUpdates.csv").toString();
        // First arg
        List<MimicMarketDataUpdate> updates = initializeUpdates(updatesFile);
        final int port = 5638;
        final String assetTreeErrorMessage = "Error occurred in VanillaAssetTree: ";
        _assetTree = new VanillaAssetTree().forServer(false);

        _serverEndpoint = new ServerEndpoint("*:" + port, _assetTree, "cluster");
        Map<String, String> marketDataMap = _assetTree.acquireMap("/adept/marketdata", String.class, String.class);

        for (; ; ) {
            // go through the updates and process
            for (MimicMarketDataUpdate update : updates) {
                System.out.println("U:" + update.getLag() + ", " + update.getKey() + ", " + update.getValue());
                Thread.sleep(update.getLag());
                marketDataMap.put(update.getKey(), update.getValue());
            }
        }
    }

    /**
     * Read from csv and update
     *
     * @param filename
     * @return
     * @throws Exception
     */
    public static List<MimicMarketDataUpdate> initializeUpdates(String filename) throws Exception {
        List<MimicMarketDataUpdate> updates = new LinkedList<>();
        long lastTimeMillis = -1;
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line = br.readLine();
            while (line != null) {
                String[] tokens = line.split(",");
                long millis = Long.valueOf(tokens[0]);
                long lag = lastTimeMillis == -1 ? 0 : millis - lastTimeMillis;
                lastTimeMillis = millis;
                String key = tokens[1];
                String value = tokens[2];
                updates.add(new MimicMarketDataUpdate(key, value, lag));
                line = br.readLine();
            }
        }
        return updates;
    }

}
