package topicsubscriptionrepro;

import net.openhft.chronicle.engine.server.ServerEndpoint;
import net.openhft.chronicle.engine.tree.VanillaAsset;
import net.openhft.chronicle.engine.tree.VanillaAssetTree;
import net.openhft.chronicle.wire.WireType;

import java.io.IOException;

public class ServerMain {
    public static final WireType WIRE_TYPE = WireType.BINARY; //TODO DS get from constructor?
    private static int _port = 5566;

    public static void main(String[] args) throws IOException {
//        YamlLogging.setAll(true);
        VanillaAssetTree assetTree = new VanillaAssetTree().forServer(Throwable::printStackTrace);
//        VanillaAssetTree assetTree = new VanillaAssetTree().forTesting();

        VanillaAsset root = assetTree.root();

        final ServerEndpoint serverEndpoint = new ServerEndpoint("*:" + _port, assetTree);

        //FIXME remove these rules and it still won't work.
        //Add wrapping rules for authorization and authentication
//        root.addWrappingRule(DdpAuthorizationKeyValueStore.class, "DDP authorization store",
//                DdpAuthorizationKeyValueStore::new, SubscriptionKeyValueStore.class);
//
//        root.addWrappingRule(AuthenticatedKeyValueStore.class, "DDP authentication store",
//                DdpAuthenticationKeyValueStore::new, DdpAuthorizationKeyValueStore.class);
//
//        //Add wrapping and leaf rules for authorization authentication subscription
//        root.addWrappingRule(DdpAuthorizationKeyValueSubscription.class, "DDP authorization subscription",
//                DdpAuthorizationKeyValueSubscription::new, VanillaKVSSubscription.class);
//
//        root.addWrappingRule(ObjectKVSSubscription.class, "DDP authentication subscription",
//                DdpAuthenticationKeyValueSubscription::new, DdpAuthorizationKeyValueSubscription.class);
//
//        root.addLeafRule(VanillaKVSSubscription.class, "Chronicle vanilla subscription", VanillaKVSSubscription::new);

        System.out.println("Press any key to exit...");
        System.in.read();
    }
}