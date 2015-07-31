package queue;

import net.openhft.chronicle.*;

public class EventPublisherAndConsumer
{
    private String _componentName;
    private EventManager _eventPublisher;
    private Chronicle _chronicleQueue;
    private boolean _enableFromChronicleLogging;
    private int _fromChronicleSleep;
    private FromChronicle<EventManager> _eventConsumer;
    private boolean _running = false;
    private EventManager _eventManagerImpl;

    public EventPublisherAndConsumer(String componentName, EventManager eventPublisher,
                                     Chronicle chronicleQueue, boolean enableFromChronicleLogging,
                                     int fromChronicleSleep, boolean isOnlyProcess)
    {
        _componentName = componentName;
        _eventPublisher = eventPublisher;
        _chronicleQueue = chronicleQueue;
        _enableFromChronicleLogging = enableFromChronicleLogging;
        _fromChronicleSleep = fromChronicleSleep;

        _eventManagerImpl = new EventManagerImpl(_componentName, _eventPublisher, isOnlyProcess);
    }

    public void start()
    {
        _eventPublisher.start(_componentName);

        _running = true;

        run();
    }

    private void run()
    {
        try
        {
            while (_running)
            {
                while (runOnce())
                {
                    // Do nothing between runs, just continue running until all events have been consumed
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public boolean runOnce() throws Exception
    {
        if (_eventConsumer == null)
        {
            _eventConsumer = FromChronicle.of(_eventManagerImpl, _chronicleQueue.createTailer(),
                    _enableFromChronicleLogging, _fromChronicleSleep);
        }

        return _eventConsumer.readOne();
    }
}