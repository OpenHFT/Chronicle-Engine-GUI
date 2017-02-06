import net.openhft.chronicle.core.io.Closeable;
import net.openhft.chronicle.engine.EngineInstance;
import net.openhft.chronicle.engine.ThreadMonitoringTest;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;

/**
 * @author Rob Austin.
 */
public class DeltaWireTest extends ThreadMonitoringTest {

    private static final String CONFIG_FILE = "replicated/client/fix-config.yaml";
    Thread lockedThread;

    @Test
    public void initiatorFailoverTest() throws Exception {

        try (@NotNull final Closeable engineSever = EngineInstance.engineMain(4, "engine.yaml")) {


        }
    }

  

}
