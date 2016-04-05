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
import net.openhft.chronicle.wire.YamlLogging;

/**
 * Created by andre on 01/05/2015.
 */
public class TextWireMain {
    public static final WireType WIRE_TYPE = WireType.TEXT;

    public static void main(String[] args) {


        YamlLogging.showServerReads(true);
        // the default is BinaryWire
        int port = 8088;
        VanillaAssetTree assetTree = new VanillaAssetTree().forTesting(false, Throwable::printStackTrace);

        final ServerEndpoint serverEndpoint = new ServerEndpoint("*:" + port, assetTree);

        if (args.length == 1 && args[0].compareTo("-debug") == 0)
        {
            System.out.println("Enabling message logging");
         }
        System.out.println("Server port seems to be " + port);
    }
}
