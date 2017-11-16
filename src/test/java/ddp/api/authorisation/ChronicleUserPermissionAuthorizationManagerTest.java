package ddp.api.authorisation;

import ddp.api.ConfigurationException;
import net.openhft.chronicle.engine.api.map.MapView;
import net.openhft.chronicle.engine.api.tree.Asset;
import net.openhft.chronicle.engine.api.tree.AssetTree;
import net.openhft.chronicle.engine.fs.Clusters;
import net.openhft.chronicle.engine.fs.EngineCluster;
import net.openhft.chronicle.engine.fs.EngineHostDetails;
import net.openhft.chronicle.engine.query.QueueConfig;
import net.openhft.chronicle.engine.server.ServerEndpoint;
import net.openhft.chronicle.engine.tree.VanillaAsset;
import net.openhft.chronicle.engine.tree.VanillaAssetRuleProvider;
import net.openhft.chronicle.engine.tree.VanillaAssetTree;
import net.openhft.chronicle.network.NetworkStats;
import net.openhft.chronicle.network.VanillaSessionDetails;
import net.openhft.chronicle.wire.WireType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.*;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

/**
 * Tests for ChronicleUserPermissionAuthorizationManager.
 */
@RunWith(Parameterized.class)
public class ChronicleUserPermissionAuthorizationManagerTest {
    private final static int TIMEOUT = 2;
    private static WireType _wireType;
    private static String _configUri = "/etc";
    private static String _chronicleEngineConfigFile = getChronicleConfigFilePath();
    private static String _domain = "mfil";
    private static String _userId = "apt_ddp";
    private static String _securityBaseUri = "/test/security";
    private static String _dataSourcePermissionsUri = "/test/security/authorization/permissions/data/source";
    private static String _cluster = "cluster";

    private static AssetTree _server1;
    private static ServerEndpoint _serverEndpoint1;
    private static int _port1 = 21111;

    private static AssetTree _server2;
    private static ServerEndpoint _serverEndpoint2;
    private static int _port2 = 21112;


    /**
     * Create a new instance with the given wire type.
     *
     * @param wireType Wire type to use for tests.
     */
    public ChronicleUserPermissionAuthorizationManagerTest(WireType wireType) {
        _wireType = wireType;
    }


    /**
     * Parameters to run an instance with. An instance of this class is run for each parameter set.
     */
    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {WireType.TEXT},
                {WireType.BINARY}
        });
    }


    /**
     * Set up necessary mocks and create servers and endpoints.
     */
    @Before
    public void setUp() throws Exception {
        _server1 = createServer(1);
        _serverEndpoint1 = createServerEndpoint(_port1, _server1, _wireType);

        _server2 = createServer(2);
        _serverEndpoint2 = createServerEndpoint(_port2, _server2, _wireType);
    }


    /**
     * Shut down server endpoints and close servers (asset trees).
     */
    @After
    public void tearDown() throws Exception {
        if (_serverEndpoint1 != null) {
            _serverEndpoint1.close();
        }

        if (_server1 != null) {
            _server1.close();
        }

        if (_serverEndpoint2 != null) {
            _serverEndpoint2.close();
        }

        if (_server2 != null) {
            _server2.close();
        }
    }


    /**
     * Create a server instance and apply necessary rules.
     *
     * @param hostId Host id of server
     * @return Asset Tree (server) instance created and configured.
     */
    private AssetTree createServer(int hostId) throws ConfigurationException, IOException {
        //Create asset tree and configure root
        VanillaAssetTree server = new VanillaAssetTree(hostId);
        server.root().addView(QueueConfig.class, new QueueConfig(s -> 1, true, null, WireType.BINARY));

        ChronicleRuleFactory.applyChronicleBasicRootAssetRules(server.root(), true);
        VanillaSessionDetails sessionDetails = VanillaSessionDetails.of(_userId, null, _domain);

        ChronicleRuleFactory.setAdminSessionDetails(server.root(), sessionDetails);

        //Install configuration
        Asset etcAsset = server.acquireAsset(_configUri);
        ChronicleRuleFactory.applyConfigAssetRules(etcAsset, _domain, _userId);

        ChronicleConfigurationLoader.loadAndInstallChronicleEngineConfiguration(server, _configUri, _chronicleEngineConfigFile);

        //Configure the security asset tree
        Asset securityAsset = server.acquireAsset(_securityBaseUri);
        ChronicleRuleFactory.applySecurityAssetRules(securityAsset, _cluster, _wireType, _domain, _userId);

        // Apply monitoring rules to urls which store monitoring information
        VanillaAsset clusterConnections = (VanillaAsset) server.acquireAsset("/proc/connections/cluster/throughput");
        server.root().getRuleProvider().configQueueServer(clusterConnections);
        @Nullable final Clusters clusters = clusterConnections.root().getView(Clusters.class);

        final EngineCluster cluster = clusters.firstCluster();

        // the reason that we have to do this is to ensure that the network stats are
        // replicated between all hosts, if you don't acquire a queue it wont exist and so
        // will not act as a slave in replication
        for (@NotNull EngineHostDetails engineHostDetails : cluster.hostDetails()) {

            final int id = engineHostDetails
                    .hostId();
            Asset asset = server.acquireAsset("/proc/connections/cluster/throughput/" + id);

            // sets the master of each of the queues
            asset.addView(new QueueConfig(x -> id, false, null, WireType.BINARY));

            server.acquireQueue("/proc/connections/cluster/throughput/" + id,
                    String.class,
                    NetworkStats.class, cluster.clusterName());
        }


        return server;
    }

    /**
     * Create a server endpoint on the given port
     */
    private ServerEndpoint createServerEndpoint(int port, AssetTree server, WireType wireType) throws IOException {
        return new ServerEndpoint("*:" + port, server, _cluster);
    }


    /**
     * Test that granting user permissions are applied both locally and replicated to the other server in the cluster.
     * Test that granting permissions on server1 are replicated to server2.
     * Test that granting permissions on server2 are replicated to server1.
     */
    @Test
    public void testGrantUserPermissions() throws Exception {
        String testSourceUri = "/test/ddp/live/data/grantuserpermissions";
        DataPermission dataPermission1 = DataPermission.ADD;
        DataPermission dataPermission2 = DataPermission.DELETE;

        //Create the two authorization managers
        ChronicleUserPermissionAuthorizationManager permissionAuthorizationManager1 = new ChronicleUserPermissionAuthorizationManager(_server1, _dataSourcePermissionsUri, null);
        ChronicleUserPermissionAuthorizationManager permissionAuthorizationManager2 = new ChronicleUserPermissionAuthorizationManager(_server2, _dataSourcePermissionsUri, null);

        //Check that initially the user is NOT authorized
        boolean isUserAuthorized = permissionAuthorizationManager1.isUserAuthorized(testSourceUri, _domain, _userId, dataPermission1, null);
        Assert.assertFalse(isUserAuthorized);

        isUserAuthorized = permissionAuthorizationManager2.isUserAuthorized(testSourceUri, _domain, _userId, dataPermission1, null);
        Assert.assertFalse(isUserAuthorized);


        //Test that granting permissions on server1 are replicated to server2.

        //Grant permissions on server1
        permissionAuthorizationManager1.grantUserPermissions(testSourceUri, _domain, _userId, dataPermission1);

        //Check that user is authorized on server1
        isUserAuthorized = permissionAuthorizationManager1.isUserAuthorized(testSourceUri, _domain, _userId, dataPermission1, null);
        Assert.assertTrue(isUserAuthorized);

        Thread.sleep(2000); //Break for replication

        //Check that user is authorized on server2 - hence replicated.
        isUserAuthorized = permissionAuthorizationManager2.isUserAuthorized(testSourceUri, _domain, _userId, dataPermission1, null);
        Assert.assertTrue(isUserAuthorized);


        //Test that granting permissions on server2 are replicated to server1.

        //Grant permissions on server2
        permissionAuthorizationManager2.grantUserPermissions(testSourceUri, _domain, _userId, dataPermission2);

        //Check that user is authorized on server2.
        isUserAuthorized = permissionAuthorizationManager2.isUserAuthorized(testSourceUri, _domain, _userId, dataPermission1, null);
        Assert.assertTrue(isUserAuthorized);

        isUserAuthorized = permissionAuthorizationManager2.isUserAuthorized(testSourceUri, _domain, _userId, dataPermission2, null);
        Assert.assertTrue(isUserAuthorized);

        Thread.sleep(2000); //Break for replication

        //Check that user is authorized with both permissions on server1
        isUserAuthorized = permissionAuthorizationManager1.isUserAuthorized(testSourceUri, _domain, _userId, dataPermission1, null);
        Assert.assertTrue(isUserAuthorized);

        //Check that user is authorized with both permissions on server1
        isUserAuthorized = permissionAuthorizationManager1.isUserAuthorized(testSourceUri, _domain, _userId, dataPermission2, null);
        Assert.assertTrue(isUserAuthorized);
    }


    /**
     * Test that revoking permissions on Server1 are replicated (revoked) on Server2, without removing other permissions.
     * Test that revoking permissions on Server2 are replicated (revoked) on Server1, without removing other permissions.
     */
    @Test
    //@Ignore("Ignoring while waiting for chronicle answer on replication bug")
    public void testRevokeUserPermissions() throws Exception
    {
        String testSourceUri = "/test/ddp/live/data/revokeuserpermissions";
        DataPermission dataPermission1 = DataPermission.ADD;
        DataPermission dataPermission2 = DataPermission.DELETE;
        BlockingQueue<String>[] subscriptions = getSubscriptions(_server1, _server2);
        //Create the two authorization managers
        ChronicleUserPermissionAuthorizationManager permissionAuthorizationManager1 = new ChronicleUserPermissionAuthorizationManager(_server1, _dataSourcePermissionsUri, null);
        ChronicleUserPermissionAuthorizationManager permissionAuthorizationManager2 = new ChronicleUserPermissionAuthorizationManager(_server2, _dataSourcePermissionsUri, null);

        //Grant permissions to be revoked
        permissionAuthorizationManager1.grantUserPermissions(testSourceUri, _domain, _userId, dataPermission1);
        permissionAuthorizationManager1.grantUserPermissions(testSourceUri, _domain, _userId, dataPermission2);

//        waitReplication(subscriptions[1], subscriptions);
        Thread.sleep(2000);

        //Check that replication is successful
        boolean isUserAuthorized = permissionAuthorizationManager2.isUserAuthorized(testSourceUri, _domain, _userId, dataPermission1, null);
        Assert.assertTrue(isUserAuthorized);
        isUserAuthorized = permissionAuthorizationManager2.isUserAuthorized(testSourceUri, _domain, _userId, dataPermission2, null);
        Assert.assertTrue(isUserAuthorized);


        //Test that revoking permissions on Server1 are replicated (revoked) on Server2, without removing other permissions.
        permissionAuthorizationManager1.revokeUserPermissions(testSourceUri, _domain, _userId, dataPermission1);
//        waitReplication(subscriptions[1], subscriptions);
        Thread.sleep(2000);

        isUserAuthorized = permissionAuthorizationManager1.isUserAuthorized(testSourceUri, _domain, _userId, dataPermission1, null);
        Assert.assertFalse(isUserAuthorized);

        isUserAuthorized = permissionAuthorizationManager2.isUserAuthorized(testSourceUri, _domain, _userId, dataPermission1, null);
        Assert.assertFalse(isUserAuthorized);

        //Check that the other permissions has NOT been removed
        isUserAuthorized = permissionAuthorizationManager1.isUserAuthorized(testSourceUri, _domain, _userId, dataPermission2, null);
        Assert.assertTrue(isUserAuthorized);

        isUserAuthorized = permissionAuthorizationManager2.isUserAuthorized(testSourceUri, _domain, _userId, dataPermission2, null);
        Assert.assertTrue(isUserAuthorized);


        //Test that revoking permissions on Server2 are replicated (revoked) on Server1, without removing other permissions.
        permissionAuthorizationManager2.revokeUserPermissions(testSourceUri, _domain, _userId, dataPermission2);
//        waitReplication(subscriptions[0], subscriptions);
        Thread.sleep(2000);
        isUserAuthorized = permissionAuthorizationManager2.isUserAuthorized(testSourceUri, _domain, _userId, dataPermission2, null);
        Assert.assertFalse(isUserAuthorized);

        isUserAuthorized = permissionAuthorizationManager1.isUserAuthorized(testSourceUri, _domain, _userId, dataPermission2, null);
        Assert.assertFalse(isUserAuthorized);
    }

    private BlockingQueue<String>[] getSubscriptions(AssetTree ... assetTrees){
        return map(assetTrees, a -> {
            LinkedBlockingDeque<String> subscription = new LinkedBlockingDeque<>(100);
            MapView<String, Map> map = a.acquireMap(_dataSourcePermissionsUri, String.class, Map.class);
            map.registerSubscriber(s -> {}
//                subscription.add(""); }
            );
            return subscription;
        }).toArray(new LinkedBlockingDeque[0]);
    }

    public static <TSource, TDest> List<TDest> map(TSource[] array, Function<TSource, TDest> mapFunction)
    {
        return map(Stream.of(array), mapFunction);
    }

    /**
     * Maps a Stream<TSource> to a List<TDest>
     *
     * @param stream      the source stream
     * @param mapFunction a function that transforms TSource to TDest
     * @param <TSource>   the source type
     * @param <TDest>     the destination type
     * @return a List<TDest>
     */
    public static <TSource, TDest> List<TDest> map(Stream<TSource> stream, Function<TSource, TDest> mapFunction)
    {
        return stream.map(mapFunction::apply).collect(toList());
    }



    private void waitReplication(BlockingQueue<String> waitOn, BlockingQueue<String>... subscriptions) throws InterruptedException {
        waitOn.poll(TIMEOUT, TimeUnit.SECONDS);
        Stream.of(subscriptions).forEach(s -> s.clear());
    }


    /**
     * @param testSourceUri Uri for which to apply PUBLISH permissions.
     * @return Map with PUBLISH permissions applied for the test user.
     */
    private Map<String, Map<String, Map<String, Set<DataPermission>>>> getTestPermissionsMap(String testSourceUri) {
        Set<DataPermission> permissionsSet = new HashSet<>();
        permissionsSet.add(DataPermission.ADD);

        Map<String, Set<DataPermission>> sourcePermissions = new HashMap<>();
        sourcePermissions.put(testSourceUri, permissionsSet);

        Map<String, Map<String, Set<DataPermission>>> userPermissions = new HashMap<>();
        userPermissions.put(_userId, sourcePermissions);

        Map<String, Map<String, Map<String, Set<DataPermission>>>> permissions = new HashMap<>();
        permissions.put(_domain, userPermissions);
        return permissions;
    }


    /**
     * @return Get the file path for the Chronicle engine configuration
     */
    private static String getChronicleConfigFilePath() {
        String path = ChronicleUserPermissionAuthorizationManagerTest.class.getProtectionDomain()
                .getCodeSource().getLocation().getPath();

        return new File(path).getParentFile().getParentFile() + "/src/test/resources/permissiontest/engine.yaml";
    }
}