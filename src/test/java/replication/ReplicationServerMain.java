package replication;

import net.openhft.chronicle.engine.api.map.MapView;
import net.openhft.chronicle.engine.api.pubsub.Publisher;
import net.openhft.chronicle.engine.api.pubsub.TopicPublisher;
import net.openhft.chronicle.engine.api.tree.Asset;
import net.openhft.chronicle.engine.fs.Clusters;
import net.openhft.chronicle.engine.fs.HostDetails;
import net.openhft.chronicle.engine.map.AuthenticatedKeyValueStore;
import net.openhft.chronicle.engine.map.ChronicleMapKeyValueStore;
import net.openhft.chronicle.engine.map.VanillaMapView;
import net.openhft.chronicle.engine.map.VanillaTopicPublisher;
import net.openhft.chronicle.engine.pubsub.VanillaReference;
import net.openhft.chronicle.engine.server.ServerEndpoint;
import net.openhft.chronicle.engine.tree.VanillaAssetTree;
import net.openhft.chronicle.threads.EventGroup;
import net.openhft.chronicle.threads.api.EventLoop;

import java.io.IOException;
import java.util.HashMap;

import static net.openhft.chronicle.engine.api.tree.RequestContext.requestContext;

/**
 * Created by Rob Austin
 */

public class ReplicationServerMain {


    private static ServerEndpoint serverEndpoint11;

    public static void main(String[] args) throws IOException {
        final Integer host = Integer.getInteger("hostId", 1);
        final VanillaAssetTree tree = new VanillaAssetTree(host);

        Asset asset = tree.root().acquireAsset(requestContext(), "map1");
        asset.addView(AuthenticatedKeyValueStore.class, new ChronicleMapKeyValueStore<>(requestContext(), asset));


        tree.root().addWrappingRule(MapView.class, "mapv view", VanillaMapView::new, AuthenticatedKeyValueStore.class);
        tree.root().addWrappingRule(TopicPublisher.class, " topic publisher", VanillaTopicPublisher::new, MapView.class);
        tree.root().addWrappingRule(Publisher.class, "publisher", VanillaReference::new, MapView.class);
        Clusters clusters = new Clusters();
        HashMap<String, HostDetails> hostDetailsMap = new HashMap<String, HostDetails>();

        {
            final HostDetails value = new HostDetails();
            value.hostId = 1;
            value.hostname = host == 1 ? "localhost" : System.getProperty("remote.host");
            value.port = 5700;
            value.timeoutMs = 1000;
            hostDetailsMap.put("host1", value);
        }
        {
            final HostDetails value = new HostDetails();
            value.hostId = 2;
            value.hostname = host == 2 ? "localhost" : System.getProperty("remote.host");
            value.port = 5700;
            value.timeoutMs = 1000;
            hostDetailsMap.put("host2", value);
        }


        clusters.put("cluster", hostDetailsMap);
        tree.root().addView(Clusters.class, clusters);


        tree.root().addView(EventLoop.class, new EventGroup(false));

        serverEndpoint11 = new ServerEndpoint(5700, tree);

    }
}

