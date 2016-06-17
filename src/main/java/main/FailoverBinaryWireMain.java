/*
 *     Copyright (C) 2015  higherfrequencytrading.com
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Lesser General Public License as published by
 *     the Free Software Foundation, either version 3 of the License.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package main;

import net.openhft.chronicle.engine.server.ServerEndpoint;
import net.openhft.chronicle.engine.tree.VanillaAssetTree;
import net.openhft.chronicle.wire.WireType;

import java.io.IOException;

/**
 * Created by andre on 01/05/2015.
 */
public class FailoverBinaryWireMain {
    public static final WireType WIRE_TYPE = WireType.BINARY;
    private static VanillaAssetTree _assetTree1;
    private static VanillaAssetTree _assetTree2;
    private static VanillaAssetTree _assetTree3;

    private final String _mapUri = "/failover/test/map1";
    private final String _server1StopKey = "K5";
    private final String _server2StopKey = "K10";
    private final String _server3StopKey = "K15";

    private FailoverBinaryWireMain() {
        _assetTree1 = new VanillaAssetTree().forServer(false );
        _assetTree2 = new VanillaAssetTree().forServer(false );
        _assetTree3 = new VanillaAssetTree().forServer(false);
    }

    public static void main(String[] args) throws IOException {
        FailoverBinaryWireMain failoverBinaryWireMain = new FailoverBinaryWireMain();
        failoverBinaryWireMain.start();
    }

    private void start() throws IOException {
        //Create test map on all servers
        _assetTree1.acquireMap(_mapUri, String.class, String.class).size();
        _assetTree2.acquireMap(_mapUri, String.class, String.class).size();
        _assetTree3.acquireMap(_mapUri, String.class, String.class).size();

        //Start endpoints
        final ServerEndpoint serverEndpoint1 = new ServerEndpoint("*:" + 9088, _assetTree1);
        final ServerEndpoint serverEndpoint2 = new ServerEndpoint("*:" + 9089, _assetTree2);
        final ServerEndpoint serverEndpoint3 = new ServerEndpoint("*:" + 9090, _assetTree3);

        //Register server stop subscribers
        _assetTree1.registerSubscriber(_mapUri + "/" + _server1StopKey + "?bootstrap=false", String.class, v -> {
            System.out.println("Stopping Server1...");
            serverEndpoint1.close();
            _assetTree1.close();
            System.out.println("Server1 stopped!");
        });

        _assetTree2.registerSubscriber(_mapUri + "/" + _server2StopKey + "?bootstrap=false", String.class, v -> {
            System.out.println("Stopping Server2...");
            serverEndpoint2.close();
            _assetTree2.close();
            System.out.println("Server2 stopped!");
        });

        _assetTree3.registerSubscriber(_mapUri + "/" + _server3StopKey + "?bootstrap=false", String.class, v -> {
            System.out.println("Stopping Server3...");
            serverEndpoint3.close();
            _assetTree3.close();
            System.out.println("Server3 stopped!");
        });

        System.out.println("Servers running, start C# unit test...");
    }
}
