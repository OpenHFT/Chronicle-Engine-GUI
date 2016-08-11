package drserver;

import net.openhft.chronicle.engine.server.ServerEndpoint;
import net.openhft.chronicle.engine.tree.VanillaAsset;
import net.openhft.chronicle.engine.tree.VanillaAssetTree;
import net.openhft.chronicle.wire.WireType;

import java.io.IOException;

public class DRServerMain {
    public static final WireType WIRE_TYPE = WireType.BINARY; //TODO DS get from constructor?
    private static int _portMain = 8888;
    private static int _portDr = 8889;

    public static void main(String[] args) throws IOException {
        /**
         * Start main server
         */
        if(args[0].equals("main")) {
            System.out.println("Starting Main Server");
            VanillaAssetTree assetTreeMain = new VanillaAssetTree().forServer();
            VanillaAsset rootMain = assetTreeMain.root();
            final ServerEndpoint serverEndpointMain = new ServerEndpoint("*:" + _portMain, assetTreeMain);
        }
        /**
         * Start DR server
         */
        else if (args[0].equals("dr")) {
            System.out.println("Starting Dr Server");
            VanillaAssetTree assetTreeDr = new VanillaAssetTree().forServer();
            VanillaAsset rootDr = assetTreeDr.root();
            final ServerEndpoint serverEndpointDr = new ServerEndpoint("*:" + _portDr, assetTreeDr);
        }


        System.out.println("Press any key to exit...");
        System.in.read();
    }
}