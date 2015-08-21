package ddp.server;

import net.openhft.chronicle.engine.api.map.*;
import net.openhft.chronicle.engine.map.*;
import net.openhft.chronicle.engine.server.*;
import net.openhft.chronicle.engine.tree.*;
import net.openhft.chronicle.network.*;
import net.openhft.chronicle.network.api.session.*;
import net.openhft.chronicle.wire.*;

import java.io.*;

public class ServerMain
{
    public static final WireType WIRE_TYPE = WireType.BINARY;
    private VanillaAssetTree _assetTree;
    private VanillaAsset _root;
    private String _mapName = "testMap";

    public static boolean _addValuesAndSubscriber = Boolean.getBoolean("add");

    public static void main(String[] args) throws IOException, InterruptedException
    {
        ServerMain serverMain = new ServerMain();
        serverMain.start();

        if (_addValuesAndSubscriber)
        {
            serverMain.addValuesAndSubscriber();
        }

        System.in.read();
    }

    public void start()
    {
        //TODO DS move to constructor
        int port = 8088;

        _assetTree = new VanillaAssetTree().forServer();

        _root = _assetTree.root();

        try
        {
            final ServerEndpoint serverEndpoint = new ServerEndpoint("*:" + port, _assetTree, WIRE_TYPE);
        }
        catch (IOException e)
        {
            //TODO DS log and change...
//            new StartupException()?
            e.printStackTrace();
        }

        _root.addWrappingRule(AuthorizedKeyValueStore.class, "authenticated kvs",
                AuthorizedKeyValueStore::new, SubscriptionKeyValueStore.class);

        //TODO DS implement
        _root.addWrappingRule(AuthenticatedKeyValueStore.class, "authenticated kvs",
                DdpAuthenticatedKeyValueStore::new, AuthorizedKeyValueStore.class);


        _root.addWrappingRule(ObjectKVSSubscription.class, "authenticated subscription",
                AuthenticatedKVSubscription::new, VanillaKVSSubscription.class);

        _root.addLeafRule(VanillaKVSSubscription.class, "DDP", VanillaKVSSubscription::new);
    }

    public void addValuesAndSubscriber() throws InterruptedException
    {
        SessionProvider sessionProvider = _root.findView(SessionProvider.class);
        sessionProvider.set(VanillaSessionDetails.of("java-daniels", "secretPwd"));

        MapView<String, String> testMap = _assetTree.acquireMap(_mapName, String.class, String.class);

        String key = "TestKey1";
        String value = "TestValue1";

        testMap.put(key, value);

        String getValue = testMap.get(key);

        System.out.println("Getting: " + getValue);

        assert value == getValue;

        testMap.registerSubscriber(x -> System.out.println("EVENT: " + x));

        Thread.sleep(200);

        key = "TestKey2";
        value = "TestValue2";

        testMap.put(key, value);

        getValue = testMap.get(key);

        System.out.println("Getting: " + getValue);

        assert value == getValue;
    }
}