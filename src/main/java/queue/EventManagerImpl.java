package queue;

public class EventManagerImpl implements EventManager
{
    private String _component;
    private EventManager _eventPublisher;
    private boolean _isOnlyProcess;
    private int _updateCounter = 0;

    public EventManagerImpl(String component, EventManager eventPublisher, boolean isOnlyProcess)
    {
        _component = component;
        _eventPublisher = eventPublisher;
        _isOnlyProcess = isOnlyProcess;
    }

    @Override
    public void start(String component)
    {
        System.out.println("START - " + component);

        if(_component.equals(component))
        {
            updatePublisher();
        }
    }

    @Override
    public void update(String component, String eventId)
    {
        if(_isOnlyProcess || !_component.equals(component))
        {
            System.out.println("UPDATE - " + component + " - id: " + eventId);

            updatePublisher();
        }
    }

    private void updatePublisher()
    {
        _updateCounter++;
        _eventPublisher.update(_component, Integer.toString(_updateCounter));
    }
}