package net.openhft.chronicle.engine.gui;

import net.openhft.chronicle.core.Jvm;
import net.openhft.chronicle.core.pool.ClassAliasPool;
import net.openhft.chronicle.engine.api.tree.RequestContext;
import net.openhft.chronicle.engine.cfg.*;
import net.openhft.chronicle.engine.fs.Clusters;
import net.openhft.chronicle.engine.fs.EngineCluster;
import net.openhft.chronicle.engine.fs.EngineHostDetails;
import net.openhft.chronicle.engine.server.ServerEndpoint;
import net.openhft.chronicle.engine.tree.TopologicalEvent;
import net.openhft.chronicle.engine.tree.VanillaAssetTree;
import net.openhft.chronicle.network.NetworkStats;
import net.openhft.chronicle.network.NetworkStatsListener;
import net.openhft.chronicle.network.cluster.HostDetails;
import net.openhft.chronicle.wire.TextWire;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Rob Austin.
 */
public class EngineMain {

    static {
        RequestContext.CLASS_ALIASES.toString();
    }

    static final Logger LOGGER = LoggerFactory.getLogger(EngineMain.class);

    static <I extends Installable> void addClass(Class<I>... iClasses) {
        ClassAliasPool.CLASS_ALIASES.addAlias(iClasses);
    }

    public static VanillaAssetTree engineMain(final int hostId) {
        try {
             //ChronicleConfig.init();
            addClass(EngineCfg.class);
          //  addClass(EngineClusterContext.class);
            addClass(JmxCfg.class);
            addClass(ServerCfg.class);
            addClass(ClustersCfg.class);
            addClass(InMemoryMapCfg.class);
            addClass(FilePerKeyMapCfg.class);
            addClass(ChronicleMapCfg.class);
            addClass(MonitorCfg.class);

            String name = "engine.yaml";
            TextWire yaml = TextWire.fromFile(name);
            EngineCfg installable = (EngineCfg) yaml.readObject();

            VanillaAssetTree assetTree = new VanillaAssetTree(hostId).forServer(false);

            try {
                installable.install("/", assetTree);
                LOGGER.info("Engine started");
            } catch (Exception e) {
                LOGGER.error("Error starting a component, stopping", e);
                assetTree.close();
            }

            final Clusters clusters = assetTree.root().getView(Clusters.class);

            if (clusters == null || clusters.size() == 0) {
                Jvm.warn().on(EngineMain.class, "cluster not found");
                return null;
            }
            if (clusters.size() != 1) {
                Jvm.warn().on(EngineMain.class, "unambiguous cluster, you have " + clusters.size() + "" +
                        " clusters which one do you want to use?");
                return null;
            }

            final EngineCluster engineCluster = clusters.firstCluster();
            final HostDetails hostDetails = engineCluster.findHostDetails(hostId);
            final String connectUri = hostDetails.connectUri();
            engineCluster.clusterContext().assetRoot(assetTree.root());

            final NetworkStatsListener networkStatsListener = engineCluster.clusterContext()
                    .networkStatsListenerFactory().apply(engineCluster
                            .clusterContext());

            final ServerEndpoint serverEndpoint = new ServerEndpoint(connectUri, assetTree, networkStatsListener);

            // we add this as close will get called when the asset tree is closed
            assetTree.root().addView(ServerEndpoint.class, serverEndpoint);

            assetTree.registerSubscriber("", TopologicalEvent.class, e -> LOGGER.info("Tree change " + e));


            // the reason that we have to do this is to ensure that the network stats are
            // replicated between all hosts, if you don't acquire a queue it wont exist and so
            // will not act as a slave in replication
            for (EngineHostDetails engineHostDetails : engineCluster.hostDetails()) {

                final int id = engineHostDetails
                        .hostId();

                assetTree.acquireQueue("/proc/connections/cluster/throughput/" + id,
                        String.class,
                        NetworkStats.class);
            }

            return assetTree;
        } catch (Exception e) {
            e.printStackTrace();
            throw Jvm.rethrow(e);
        }

    }

    public static void main(String[] args) {
        engineMain(2);
    }

}
