package queue.replication;


import net.openhft.chronicle.engine.EngineInstance;
import net.openhft.chronicle.engine.cfg.EngineClusterContext;
import net.openhft.chronicle.engine.tree.ChronicleQueueView;
import net.openhft.chronicle.engine.tree.VanillaAssetTree;
import net.openhft.chronicle.network.TCPRegistry;
import net.openhft.chronicle.queue.ExcerptAppender;
import net.openhft.chronicle.queue.ExcerptTailer;
import net.openhft.chronicle.queue.impl.RollingChronicleQueue;
import net.openhft.chronicle.wire.DocumentContext;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertEquals;

/**
 * Created by rob on 25/06/2017.
 */
public class QueueReplicationTest {

    public static final String expected = "hello world";

    @Test
    public void test() throws IOException {
        TCPRegistry.createServerSocketChannelFor("host.port");// allocates random port
        TCPRegistry.createServerSocketChannelFor("host.port2");// allocates random port

        Application.deleteFile(new File("queue/data1"));
        Application.deleteFile(new File("queue/data2"));

        Application.addClass(EngineClusterContext.class);
        //  Application.addClass(SampleEntity.class);

        try (VanillaAssetTree tree1 = EngineInstance.engineMain(1, "src/test/resources/engineConfig-Q1.yaml")) {

            ChronicleQueueView qv1 = (ChronicleQueueView) tree1.acquireQueue("/queue/data", String.class, String.class, "clusterTwo2");
            RollingChronicleQueue rollingChronicleQueue1 = qv1.chronicleQueue();
            ExcerptAppender appender = rollingChronicleQueue1.acquireAppender();

            try (DocumentContext dc = appender.writingDocument()) {
                dc.wire().getValueOut().text(expected);
            }

            try (VanillaAssetTree tree2 = EngineInstance.engineMain(2, "src/test/resources/engineConfig-Q2.yaml")) {
                ChronicleQueueView qv2 = (ChronicleQueueView) tree2.acquireQueue("/queue/data", String.class, String.class, "clusterTwo2");
                RollingChronicleQueue rollingChronicleQueue2 = qv2.chronicleQueue();
                ExcerptTailer tailer = rollingChronicleQueue2.createTailer();

                for (; ; ) {
                    try (DocumentContext dc = tailer.readingDocument()) {
                        if (!dc.isPresent())
                            continue;
                        if (!dc.isData()
                                )
                            continue;
                        String actual = dc.wire().getValueIn().text();
                        assertEquals(expected, actual);
                        break;
                    }
                }
            }
        }
    }
}
