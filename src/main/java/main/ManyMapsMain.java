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

import ddp.api.TestUtils;
import net.openhft.chronicle.engine.api.tree.AssetTree;
import net.openhft.chronicle.engine.tree.VanillaAssetTree;
import net.openhft.chronicle.wire.WireType;
import org.junit.Assert;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

/**
 * Created by peter on 18/08/15.
 */
public class ManyMapsMain {
    static final String hostname = System.getProperty("hostname", "localhost");
    private static int _noOfMaps = 1_100;
    private static int _noOfKvps = 1_000;
    private static String _mapBaseName = "ManyMapsTest-";

    public static void main(String[] args) {
        AssetTree clientAssetTree = new VanillaAssetTree(11).forRemoteAccess("localhost:" +
                BinaryWireMain.PORT, WireType.BINARY, Throwable::printStackTrace);
        System.out.println("Creating maps.");
        AtomicInteger count = new AtomicInteger();
        Map<String, Map<String, String>> _clientMaps = new HashMap<>();
        IntStream.rangeClosed(1, _noOfMaps).forEach(i -> {
            String mapName = _mapBaseName + i;

            Map<String, String> map = clientAssetTree.acquireMap(mapName, String.class, String.class);

            for (int j = 1; j <= _noOfKvps; j++) {
                map.put(TestUtils.getKey(mapName, j), TestUtils.getValue(mapName, j));
            }
            Assert.assertEquals(_noOfKvps, map.size());

            _clientMaps.put(mapName, map);
            if (count.incrementAndGet() % 100 == 0)
                System.out.print("... " + count);
        });
        System.out.println("...client maps " + _noOfMaps + " Done.");

        //Test that the number of maps created exist
        Assert.assertEquals(_noOfMaps, _clientMaps.size());

        for (Map.Entry<String, Map<String, String>> mapEntry : _clientMaps.entrySet()) {
            System.out.println(mapEntry.getKey());
            Map<String, String> map = mapEntry.getValue();

            //Test that the number of key-value-pairs in the map matches the expected.
            Assert.assertEquals(_noOfKvps, map.size());
        }
        clientAssetTree.close();

    }
}
