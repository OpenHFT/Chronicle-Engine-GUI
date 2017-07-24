package queue;

import queue4.EventManager;
import queue4.externalizableObjects.ConfigSetting;
import queue4.externalizableObjects.MarketDataSource;
import queue4.externalizableObjects.MarketDataSupplier;
import queue4.externalizableObjects.MarketDataType;

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

    public void start(String component)
    {
        System.out.println("START - " + component);

        if(_component.equals(component))
        {
            updatePublisher();
        }
    }

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
        //_eventPublisher.update(_component, Integer.toString(_updateCounter));
    }

    @Override
    public void getConfig(String executor) {

    }

    @Override
    public void setConfig(String executor) {

    }

    @Override
    public void onConfigAdd(String executor, ConfigSetting addedConfigSetting) {

    }

    @Override
    public void onConfigUpdate(String executor, ConfigSetting updateConfigSetting) {

    }

    @Override
    public void onConfigRemove(String executor, ConfigSetting removedConfigSetting) {

    }

    @Override
    public void getMarketData(String executor) {

    }

    @Override
    public void setMarketData(String executor) {

    }

    @Override
    public void onMarketDataUpdate(String producer, MarketDataSupplier supplier, MarketDataSource source, MarketDataType type, String id, byte[] marketDataUpdates, boolean isRetransmit) throws Exception {

    }

    @Override
    public void process(String executor) {

    }

    @Override
    public boolean hasChanged() {
        return false;
    }

    @Override
    public boolean isInitialized() {
        return false;
    }

    @Override
    public String getExecutor() {
        return null;
    }
}