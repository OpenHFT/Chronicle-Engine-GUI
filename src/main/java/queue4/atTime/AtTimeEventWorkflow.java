package queue4.atTime;

import net.openhft.chronicle.queue.ExcerptTailer;
import net.openhft.chronicle.queue.TailerDirection;
import net.openhft.chronicle.queue.impl.single.SingleChronicleQueue;
import net.openhft.chronicle.wire.DocumentContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import queue.EventWorkflow;
import queue4.EventManager;
import queue4.chronicle.FromChronicle;

/**
 * Deals with reading in events from the existing Chronicle Queue until none are left to read.
 * The provided AtTimeEventManager deals with what to do when "atTime" is encountered
 * (e.g. save a valuation environment or market data to file, etc.)
 */
public class AtTimeEventWorkflow implements EventWorkflow {
    private final Logger _logger = LogManager.getLogger(AtTimeEventWorkflow.class);

    private final SingleChronicleQueue _inputChronicle;
    private final EventManager _atTimeEventManager;
    private FromChronicle<EventManager> _reader;


    /**
     * @param inputChronicle     The Chronicle to use for input.
     * @param atTimeEventManager The AtTimeEventManager that will execute a specific action at a specific time.
     */
    public AtTimeEventWorkflow(SingleChronicleQueue inputChronicle, EventManager atTimeEventManager) {
        _inputChronicle = inputChronicle;
        _atTimeEventManager = atTimeEventManager;
    }


    /**
     * @see EventWorkflow
     */
    @Override
    public void start() {
        ExcerptTailer tailer = _inputChronicle.createTailer();
        // skip to the end of the queue
        long lastValidIndex = Long.MIN_VALUE;
        int i = 0;
        while (true) {
            try (final DocumentContext ctx = tailer.readingDocument()) {
                if (!ctx.isPresent()) {
                    break;
                }
                if (i == 154) {
                    lastValidIndex = tailer.index();
                }
            }
            System.out.println("Found document " + (i++));
        }
        // now change tailer direction and move to 'last known good' index
        System.out.printf("Moved? %s%n", tailer.moveToIndex(lastValidIndex));
        tailer.direction(TailerDirection.BACKWARD);
        _reader = FromChronicle.of(_atTimeEventManager, tailer);
    }


    /**
     * @see EventWorkflow
     */
    @Override
    public void stop() {
        _atTimeEventManager.stopped(null);
    }


    /**
     * @see EventWorkflow
     */
    @Override
    public String getExecutor() {
        return _atTimeEventManager.getExecutor();
    }


    /**
     * @see EventWorkflow
     */
    @Override
    public boolean runOnce() {
        try {
            return _reader.readOne();
        } catch (Exception e) {
            _logger.error("Exception occurred while reading from Chronicle.", e);
            return false;
        }
    }


    /**
     * @see EventWorkflow
     */
    @Override
    public boolean onIdle() {
        return false;
    }
}