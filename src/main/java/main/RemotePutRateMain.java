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
import net.openhft.chronicle.engine.tree.VanillaAssetTree;
import net.openhft.chronicle.wire.WireType;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.stream.IntStream;

/**
 * Created by peter on 20/08/15.
 */
public class RemotePutRateMain {
    private static final String _testStringFilePath = "Vols" + File.separator + "USDVolValEnvOIS-BO.xml";
    private static final String _mapName = "PerfTestMap-puts";
    private static final int _noOfPuts = 50;
    private static final int _noOfRunsToAverage = 10;
    private static final long _secondInNanos = 1_000_000_000L;
    private static final String hostname = System.getProperty("hostname", "localhost");
    private static String _twoMbTestString;
    private static int _twoMbTestStringLength;

    public static void main(String[] args) throws IOException, URISyntaxException {
        _twoMbTestString = TestUtils.loadSystemResourceFileToString(_testStringFilePath);
        _twoMbTestStringLength = _twoMbTestString.length();

        VanillaAssetTree clientAssetTree = new VanillaAssetTree(13).forRemoteAccess(hostname + ":" + BinaryWireMain.PORT, WireType.BINARY);

        Map<String, String> _testMap = clientAssetTree.acquireMap(_mapName, String.class, String.class);

        System.out.println("Clearing the map");
        _testMap.clear();

        System.out.println("Running tests");
        //Perform test a number of times to allow the JVM to warm up, but verify runtime against average
        TestUtils.runMultipleTimesAndVerifyAvgRuntime(i -> _testMap.size(), () -> {
            IntStream.range(0, _noOfPuts).forEach(i ->
                    _testMap.put(TestUtils.getKey(_mapName, i), _twoMbTestString));
        }, _noOfRunsToAverage, _secondInNanos);

        clientAssetTree.close();
    }
}
