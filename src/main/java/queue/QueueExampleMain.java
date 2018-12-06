package queue;

import com.sun.jna.platform.win32.*;
import net.openhft.chronicle.*;
import queue4.EventManager;

import java.io.*;
import java.nio.file.*;

public class QueueExampleMain
{
    public static void main(String[] args) throws IOException
    {
        String componentId = Guid.GUID.newGuid().toGuidString();

        String queuePath = System.getProperty("chronicle.queuePath", "C:\\LocalFolder\\temp\\Chronicle");

        boolean recreateQueueDir = Boolean.getBoolean("recreateQueueDir");

        boolean isOnlyProcess = Boolean.getBoolean("isOnlyProcess");

        boolean enableToChronicleDebuggingLog = Boolean.getBoolean("toChronicle.enableLogging");
        boolean enableFromChronicleDebuggingLog = Boolean.getBoolean("fromChronicle.enableLogging");

        boolean useSameChronicleInstance = Boolean.getBoolean("useSameChronicleInstance");

        int toChronicleSleep = Integer.getInteger("toChronicle.sleep", 0);
        int fromChronicleSleep = Integer.getInteger("fromChronicle.sleep", 0);

        System.out.println("starting with the following settings:");
        System.out.println("recreateQueueDir: " + recreateQueueDir);
        System.out.println("isOnlyProcess: " + isOnlyProcess);
        System.out.println("chronicle.queuePath: " + queuePath);
        System.out.println("toChronicle.enableLogging: " + enableToChronicleDebuggingLog);
        System.out.println("fromChronicle.enableLogging: " + enableFromChronicleDebuggingLog);
        System.out.println("useSameChronicleInstance: " + useSameChronicleInstance);
        System.out.println("toChronicle.sleep: " + toChronicleSleep);
        System.out.println("fromChronicle.sleep: " + fromChronicleSleep);
        System.out.println("######");
        System.out.println("Component id: " + componentId);
        System.out.println("######");

        if (recreateQueueDir)
        {
            recreateDirectory(queuePath);
        }

        Chronicle toChronicleQueue = ChronicleQueueBuilder.vanilla(queuePath).build();

        EventManager eventManager = ToChronicle.of(EventManager.class, toChronicleQueue, enableToChronicleDebuggingLog, toChronicleSleep);

        EventPublisherAndConsumer eventPublisherAndConsumer;

        if (useSameChronicleInstance)
        {
            eventPublisherAndConsumer = new EventPublisherAndConsumer(componentId,
                    eventManager, toChronicleQueue, enableFromChronicleDebuggingLog,
                    fromChronicleSleep, isOnlyProcess);
        }
        else
        {
            Chronicle fromChronicleQueue = ChronicleQueueBuilder.vanilla(queuePath).build();

            eventPublisherAndConsumer = new EventPublisherAndConsumer(componentId,
                    eventManager, fromChronicleQueue, enableFromChronicleDebuggingLog,
                    fromChronicleSleep, isOnlyProcess);
        }

        eventPublisherAndConsumer.start();
    }

    private static void recreateDirectory(String folderPath) throws IOException
    {
        File directory = new File(folderPath);

        System.out.println("Re-creating dir " + directory.getPath());

        deleteDirectory(new File(folderPath));

        Files.createDirectory(directory.toPath());
    }

    private static boolean deleteDirectory(File fileOrDir)
    {
        if (fileOrDir.isDirectory())
        {
            File[] children = fileOrDir.listFiles();

            for (int i = 0; i < children.length; i++)
            {
                boolean success = deleteDirectory(children[i]);

                if (!success)
                {
                    return false;
                }
            }
        }

        System.out.println("Deleting: " + fileOrDir.getPath());

        return fileOrDir.delete();
    }
}