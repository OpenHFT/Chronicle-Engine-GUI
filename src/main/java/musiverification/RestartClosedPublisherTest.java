package musiverification;


import net.openhft.chronicle.core.*;
import net.openhft.chronicle.engine.server.*;
import net.openhft.chronicle.engine.tree.*;
import net.openhft.chronicle.network.*;
import net.openhft.chronicle.wire.*;
import org.jetbrains.annotations.*;
import org.junit.*;

import java.util.*;
import java.util.concurrent.*;

public class RestartClosedPublisherTest
{
    public static final WireType WIRE_TYPE = WireType.TEXT;
    private static final String CONNECTION_1 = "Test1.host.port";
    private ServerEndpoint _serverEndpoint1;
    private VanillaAssetTree _server;
    private VanillaAssetTree _remote;
    private String _testMapUri = "/test/map";

    @Before
    public void setUp() throws Exception
    {
        TCPRegistry.createServerSocketChannelFor(CONNECTION_1);
        _server = new VanillaAssetTree().forServer();

        _serverEndpoint1 = new ServerEndpoint(CONNECTION_1, _server, WIRE_TYPE);

        createRemoteClient();
    }

    private void createRemoteClient()
    {
        _remote = new VanillaAssetTree().forRemoteAccess(CONNECTION_1, WIRE_TYPE);
    }

    /**
     * Test that a client can connect to a server side map, register a subscriber and perform put. Close the client,
     * create new one and do the same.
     */
    @Test
    public void testClientReconnectionOnMap() throws InterruptedException
    {
        String testKey = "Key1";
        String value = "Value1";

        BlockingQueue<String> eventQueue = new ArrayBlockingQueue<>(1);

        connectClientAndPerformPutGetTest(testKey, value, eventQueue);

        _remote.close();
        Jvm.pause(200);

        value = "Value2";
        connectClientAndPerformPutGetTest(testKey, value, eventQueue);
    }

    @NotNull
    private void connectClientAndPerformPutGetTest(String testKey, String value, BlockingQueue<String> eventQueue) throws InterruptedException
    {
        _remote = new VanillaAssetTree().forRemoteAccess(CONNECTION_1, WIRE_TYPE);

        String keySubUri = _testMapUri + "/" + testKey + "?bootstrap=false";
        Map<String, String> map = _remote.acquireMap(_testMapUri, String.class, String.class);
        map.size();
        _remote.registerSubscriber(keySubUri + "?bootstrap=false", String.class, eventQueue::add);

        map.put(testKey, value);

        Assert.assertEquals(value, eventQueue.poll(200, TimeUnit.MILLISECONDS));
        String getValue = map.get(testKey);
        Assert.assertEquals(value, getValue);
    }
}