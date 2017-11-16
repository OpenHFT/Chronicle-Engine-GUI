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

import net.openhft.chronicle.core.OS;
import net.openhft.chronicle.engine.api.map.KeyValueStore;
import net.openhft.chronicle.engine.api.map.MapView;
import net.openhft.chronicle.engine.map.ChronicleMapKeyValueStore;
import net.openhft.chronicle.engine.map.VanillaMapView;
import net.openhft.chronicle.engine.server.ServerEndpoint;
import net.openhft.chronicle.engine.tree.VanillaAssetTree;
import net.openhft.chronicle.wire.WireType;
import net.openhft.chronicle.wire.YamlLogging;

import org.apache.commons.cli.*;

import java.io.IOException;

/**
 * Created by andre on 01/05/2015.
 */
public class BinaryWireMain {

    public static final net.openhft.chronicle.wire.WireType WIRE_TYPE = WireType.BINARY;
    public static final boolean PERSIST_TO_CHRONICLE = Boolean.parseBoolean(System.getProperty("persisted", "true"));
    public static final int PORT = 9090;

    public static void main(String[] args) throws IOException {
        int port = PORT;

        CommandLine commandLine;
        Options options = new Options();
        options.addOption("d", "debug", false, "Debug enabled");
        Option portOpt = Option.builder("p")
                .longOpt( "port" )
                .desc( "port number to use instead of the default"  )
                .hasArg()
                .build();
        options.addOption(portOpt);
        CommandLineParser parser = new DefaultParser();
        try {
            // parse the command line arguments
            CommandLine line = parser.parse( options, args );
            if (line.hasOption(portOpt.getOpt()))
            {
                port = Integer.parseInt(line.getOptionValue(portOpt.getOpt()));
                System.out.println("Overriding port with " + port);
            }
            if (line.hasOption("debug")) {
                System.out.println("Enabling message logging");
                YamlLogging.showServerReads(true);
                YamlLogging.showServerWrites(true);
            }
        }
        catch( ParseException exp ) {
            // oops, something went wrong
            System.err.println( "Parsing failed.  Reason: " + exp.getMessage() );
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp( "MUFG", options );
            System.exit(1);
        }

        VanillaAssetTree assetTree = new VanillaAssetTree().forTesting(false);

        if(PERSIST_TO_CHRONICLE) {
            System.out.println("Running with persistence");
            assetTree.root().addWrappingRule(MapView.class, "map directly to KeyValueStore",
                    VanillaMapView::new, KeyValueStore.class);
            assetTree.root().addLeafRule(KeyValueStore.class, "use Chronicle Map", (context, asset) -> {
                context.basePath(OS.TARGET)
                        .putReturnsNull(!context.name().startsWith("subscribeConcurrent") && !context.name().startsWith("group")
                                && !context.name().startsWith("testSubscriptionMapEventOnAllKeys"))
                                //.putReturnsNull(false)
                        .entries(50);
                if(context.valueType() == String.class && !context.name().startsWith("ManyMapsTest"))
                    context.averageValueSize(2e6);
                return new ChronicleMapKeyValueStore(context, asset);
            });
        }

        final ServerEndpoint serverEndpoint = new ServerEndpoint("*:" + port, assetTree, "cluster");

        System.out.println("Server port seems to be " + port);
    }
}
