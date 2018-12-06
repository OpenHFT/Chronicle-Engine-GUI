package queue4;

import net.openhft.chronicle.queue.impl.single.SingleChronicleQueue;
import net.openhft.chronicle.queue.impl.single.SingleChronicleQueueBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import queue4.atTime.AtTimeEventManager;
import queue4.atTime.AtTimeEventWorkflow;
import queue4.atTime.BooleanFunctionCallRecorder;
import queue4.atTime.ValuationEnvironmentSaver;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by hardikd on 20/07/2017.
 */
public class AtTimeTest {
    private final static Logger LOGGER = LogManager.getLogger(AtTimeTest.class);
    private static String VALUATION_ENVIRONMENT_DIRECTORY;
    private static String VALUATION_ENVIRONMENT_QUEUE;


    /**
     * Called before any test are run.
     */
    @BeforeClass
    public static void setUp()
    {

        String workingDirectory = System.getProperty("user.dir");

        VALUATION_ENVIRONMENT_QUEUE = Paths.get(workingDirectory, "src/test/resources/ValuationEnvironment").toString();
        VALUATION_ENVIRONMENT_DIRECTORY = Paths.get(workingDirectory, "src/test/resources").toString();


    }


    /**
     * Tests if the Valuation Environment is saved to file when the task is run.
     *
     * @throws Exception
     */
    @Test
    public void testValuationEnvironmentSavingToFile() throws Exception
    {
        // Specify the IDs/names to use

        String taskName = "SaveValuationEnvironmentTask";
        String valuationEnvironmentName = "ValEnv";
        String valuationEnvironmentMarketDataId = "USD OIS";
        String executor = "NY01 USD IRS";
        String executorId = "1";
        ZoneId zoneId = ZoneId.of("UTC");

        String ddpWebServiceUrlString = "http://ddp-rel-uk1.mfil.local.:5751/ws/timeseriesprotoservice?wsdl";
        String ddpWebServiceNamespaceUri = "http://timeseries.services.ddp/";
        String ddpWebServiceLocalPart = "TimeSeriesProtoWebService";
        String ddpTimeSeriesIdentifier = "/ddp/data/timeseries/ln/server/market/ir/curve/ois/usd/obj/ln_cob";



        // Generate the system and previous business dates
        ZonedDateTime systemDate = ZonedDateTime.of(2017, 05, 26, 0, 0, 0, 0, zoneId).with(LocalTime.of(0, 0, 0, 0));

        /**
         * We have a valuation file which contains valuation environments published between 26 May 2017 09:27 and 26 May 2017 09:37
         * We are trying to snap the valuation environment just before 26 May 2017 09:30 which is what the "atTime" is set to.
         */
        ZonedDateTime atDate = ZonedDateTime.of(2017, 05, 26, 9, 30, 0, 0, zoneId);


        // Generate the names of the valuation environment and the archive directory

        String valuationEnvironmentPrefix = "USD_OISValEnv-";
        String valuationEnvironmentSuffix = "-COB.xml";
        String valuationEnvironmentFilename = valuationEnvironmentPrefix + "201705" + valuationEnvironmentSuffix;
        String archiveFolderName = "201705";
        Path valuationEnvironmentFilePath = Paths.get(VALUATION_ENVIRONMENT_DIRECTORY, archiveFolderName, valuationEnvironmentFilename);

        // Create the archive folder to store the valuation environment in

        Path archiveFolder = valuationEnvironmentFilePath.getParent();
        Files.createDirectories(archiveFolder);
        Assert.assertTrue(Files.exists(archiveFolder));
        Assert.assertTrue(Files.isDirectory(archiveFolder));
        String valuationEnvironmentDirectory = archiveFolder.getParent().toFile().getAbsolutePath();
        BooleanFunctionCallRecorder functionCallRecorder = new BooleanFunctionCallRecorder();

        try
        {

            LOGGER.info("Connecting to the valuation environment Chronicle queue.");
            try (SingleChronicleQueue valuationEnvironmentChronicle = SingleChronicleQueueBuilder.binary(VALUATION_ENVIRONMENT_QUEUE).build())
            {


                //Create the ValuationEnvironmentSaver which will save the file as per specified parameters
                ValuationEnvironmentSaver valuationEnvironmentSaver = new ValuationEnvironmentSaver(functionCallRecorder);

                //EventManager and Workflow executor to deal with iterating over Chronicle and saving the valuation environment to a file
                AtTimeEventManager atTimeEventManager = new AtTimeEventManager(valuationEnvironmentSaver, atDate.toInstant().toEpochMilli(), executor, valuationEnvironmentMarketDataId);
                AtTimeEventWorkflow atTimeEventWorkflowExecutor = new AtTimeEventWorkflow(valuationEnvironmentChronicle, atTimeEventManager);

                //Start the workflow
                atTimeEventWorkflowExecutor.start();

                //Run the workflow until all events consumed
                while (atTimeEventWorkflowExecutor.runOnce())
                {
                    // Do nothing between runs, just continue running until all events have been consumed
                }

                //Stop the workflow
                atTimeEventWorkflowExecutor.stop();
            }
        }
        catch (Exception e)
        {
            LOGGER.error("Exception occurred while attempting to update the save closing valuation environment for the executor \"{}\".", executor, e);
        }

        // Test that the file was created
        Assert.assertTrue(functionCallRecorder.getfunctionCallRecieved());

    }

}
