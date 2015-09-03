package topicsubscriptionrepro;

import net.openhft.chronicle.engine.tree.*;
import net.openhft.chronicle.network.*;
import net.openhft.chronicle.wire.*;

import java.io.*;
import java.util.*;

public class ConstructorExceptionClient
{
    private static final WireType _wireType = WireType.BINARY;
    private static VanillaAssetTree _assetTree;
    private static String _serverAddress = "localhost:5566";

    private static String _throwInConstructorMapName = "/throw/in/constructor/map";

    public static void main(String[] args) throws IOException, InterruptedException
    {
        _assetTree = new VanillaAssetTree();

        _assetTree.root().forRemoteAccess(
                new String[]{_serverAddress}, _wireType,
                VanillaSessionDetails.of("mfil-daniels", null));

        try
        {
            //Constructor is called at this point on server and exception thrown!
            Map<String, String> map = _assetTree.acquireMap(_throwInConstructorMapName, String.class, String.class);

            //TODO gets stuck here
            System.out.println("Getting map size...");

            int size = map.size();

            System.out.println("Got map size: " + size);

            //TODO should not reach this point
            String key = "Key1";
            String value = "Key1";

            System.out.println("Putting kvp...");
            map.put(key, value);

            System.out.println("Getting kvp...");
            String getValue = map.get(key);
            System.out.println("Got value: " + getValue);

            if (getValue != null && getValue.equals(value))
            {
                System.err.println("Managed to get value from Map which shouldn't exist!");
            }

            System.err.println("Made it to the end, when we should have got an exception!");
        }
        catch (Exception e)
        {
            //TODO expect exception!

            System.out.println("Exception as expected.");

            System.exit(-1);
        }

        System.err.println("DID NOT get Exception as expected!");

        System.out.println("Press any key to exit...");
        System.in.read();
    }
}