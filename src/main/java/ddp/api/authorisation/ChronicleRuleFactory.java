package ddp.api.authorisation;

import net.openhft.chronicle.core.threads.EventLoop;
import net.openhft.chronicle.engine.api.EngineReplication;
import net.openhft.chronicle.engine.api.map.MapView;
import net.openhft.chronicle.engine.api.map.SubscriptionKeyValueStore;
import net.openhft.chronicle.engine.api.pubsub.Replication;
import net.openhft.chronicle.engine.api.set.EntrySetView;
import net.openhft.chronicle.engine.api.tree.Asset;
import net.openhft.chronicle.engine.api.tree.LeafViewFactory;
import net.openhft.chronicle.engine.map.*;
import net.openhft.chronicle.engine.session.VanillaSessionProvider;
import net.openhft.chronicle.engine.tree.HostIdentifier;
import net.openhft.chronicle.engine.tree.TopologySubscription;
import net.openhft.chronicle.engine.tree.VanillaReplication;
import net.openhft.chronicle.engine.tree.VanillaTopologySubscription;
import net.openhft.chronicle.network.api.session.SessionDetails;
import net.openhft.chronicle.network.api.session.SessionProvider;
import net.openhft.chronicle.threads.EventGroup;
import net.openhft.chronicle.threads.Threads;
import net.openhft.chronicle.wire.WireType;

import java.util.Map;

/**
 * Created by hardikd on 06/01/2017.
 */
public class ChronicleRuleFactory {

    /**
     * Configures the Chronicle root with any common rules for all sub asset trees.
     *
     * @param root        Root asset common for all.
     * @param runAsDaemon Bool indicating whether or not to run as daemon. Should only be true for testing.
     */
    public static void applyChronicleBasicRootAssetRules(Asset root, boolean runAsDaemon)
    {
        //Replication
        root.addLeafRule(EngineReplication.class, "Engine replication holder", CMap2EngineReplicator::new);

        String fullName = root.fullName();
        HostIdentifier hostIdentifier = root.findView(HostIdentifier.class);
        if (hostIdentifier != null)
        {
            fullName = "tree-" + hostIdentifier.hostId() + fullName;
        }

        ThreadGroup threadGroup = new ThreadGroup(fullName);
        root.addView(ThreadGroup.class, threadGroup);
        LeafViewFactory<EventLoop> eventLoopLeafRule = (rc, asset) ->
                Threads.withThreadGroup(threadGroup, () ->
                {

                    EventLoop eg = new EventGroup(runAsDaemon);
                    eg.start();
                    return eg;
                });
        root.addLeafRule(EventLoop.class, " event group", eventLoopLeafRule);
    }

    /**
     * Registers the session details on the given asset, both wrapped in a {@link SessionProvider} and as a stand alone
     * {@link SessionDetails}
     *
     * @param assetToSetOn   Asset on which to register the {@link SessionDetails}.
     * @param sessionDetails {@link SessionDetails} to register.
     */
    public static void setAdminSessionDetails(Asset assetToSetOn, SessionDetails sessionDetails)
    {
        VanillaSessionProvider vanillaSessionProvider = (VanillaSessionProvider) assetToSetOn.findView(SessionProvider.class);

        if (vanillaSessionProvider == null)
        {
            vanillaSessionProvider = new VanillaSessionProvider();
            assetToSetOn.addView(SessionProvider.class, vanillaSessionProvider); //Need to be set as it is used by custom interfaces
        }

        vanillaSessionProvider.set(sessionDetails);
        assetToSetOn.addView(SessionDetails.class, sessionDetails); //Used for replication user details
    }

    /**
     * Applies Chronicle wrapping rules for configuration assets for which only the user with given
     * domain/userid has access to.
     *
     * @param configAsset Asset to configure as a single user configuration asset.
     * @param adminDomain Domain for admin user allowed access to the configuration assets.
     * @param adminUser   User id for admin user allowed access to the configuration assets.
     */
    public static void applyConfigAssetRules(Asset configAsset, String adminDomain, String adminUser)
    {
        configAsset.addWrappingRule(MapView.class, "string key maps", VanillaMapView::new, ObjectKeyValueStore.class);

        configAsset.addWrappingRule(ObjectKeyValueStore.class, "chronicle authenticated",
                VanillaSubscriptionKeyValueStore::new, AuthenticatedKeyValueStore.class);

        configAsset.addLeafRule(AuthenticatedKeyValueStore.class, "chronicle vanilla", VanillaKeyValueStore::new);

        configAsset.addLeafRule(ObjectSubscription.class, "chronicle vanilla", MapKVSSubscription::new);

        configAsset.addLeafRule(TopologySubscription.class, "chronicle vanilla", VanillaTopologySubscription::new);
    }


    /**
     * Configure security asset tree to be single user (admin user only) with replication to other DataHubManagers
     * using Chronicle in-memory map.
     *
     * @param securityAsset Asset to configure as with the security rules.
     * @param clusterName   Name of cluster to use for security replication.
     * @param wireType      Wire type to be used for security maps.
     * @param adminDomain   Domain for admin user allowed access to the configuration assets.
     * @param adminUser     User id for admin user allowed access to the configuration assets.
     */
    public static void applySecurityAssetRules(Asset securityAsset, String clusterName, WireType wireType,
                                               String adminDomain, String adminUser)
    {
        //Configure basic rules required in addition to the root rules
        securityAsset.addWrappingRule(Replication.class, "chronicle replication", VanillaReplication::new, MapView.class);
        securityAsset.addWrappingRule(EntrySetView.class, "chronicle entrySet", VanillaEntrySetView::new, MapView.class);

        //Map wrapping rules
        securityAsset.addWrappingRule(MapView.class, "map view",
                (context, assetInput, kvStore) -> new VanillaMapView(context.putReturnsNull(false), assetInput, kvStore),
                SubscriptionKeyValueStore.class);

        //Add rules to ensure the use of Chronicle in-memory replicated map
        securityAsset.addLeafRule(SubscriptionKeyValueStore.class, "use Chronicle In-Memory Map", (context, assetInput) ->
        {
            context = context
                    .putReturnsNull(false)
                    .entries(6) //Leaving a bit of head room as this should be equal to the number of domains
                    .wireType(wireType)
                    .keyType(String.class)
                    .valueType(Map.class)
                    .averageValueSize(10485760) // 10 mb - should be able to contain all permissions for a given domain
                    .cluster(clusterName); // Not persisted by default

            ChronicleMapKeyValueStore chronicleMapKeyValueStore = new ChronicleMapKeyValueStore(context, assetInput);
            return chronicleMapKeyValueStore;
        });

        securityAsset.addLeafRule(ObjectSubscription.class, "chronicle vanilla", MapKVSSubscription::new);
    }

}
