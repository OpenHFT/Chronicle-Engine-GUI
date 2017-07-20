package queue4.atTime;


import queue4.EventManager;
import queue4.MarketDataInputByteBuffer;
import queue4.externalizableObjects.ConfigSetting;
import queue4.externalizableObjects.MarketDataSource;
import queue4.externalizableObjects.MarketDataSupplier;
import queue4.externalizableObjects.MarketDataType;

/**
 * This is an event manager that is used as part of the "at time" option.
 * <p>
 * It stores a single version of market data entry, overwriting it continuously, until "atTime" and then immediately executes a specified action.
 */
public class AtTimeEventManager implements EventManager
{
    private final AtTimeMarketDataUpdateCallback _atTimeMarketDataUpdateCallback;
    private final String _producer;
    private final String _valuationEnvironmentMarketDataId;



    /**
     * @param atMillis The number of seconds since the EPOCH that marks the period of time of interest.
     */
    public AtTimeEventManager(ValuationEnvironmentSaver valuationEnvironmentSaver, long atMillis, String executor, String valuationEnvironmentMarketDataId)
    {
        _atTimeMarketDataUpdateCallback = new AtTimeMarketDataUpdateCallback(valuationEnvironmentSaver, atMillis);
        _atTimeMarketDataUpdateCallback.setMarketDataType(MarketDataType.VALUATION_ENVIRONMENT);
        _producer = executor;
        _valuationEnvironmentMarketDataId = valuationEnvironmentMarketDataId;
    }


    /**
     * @see EventManager
     */
    @Override
    public void getConfig(String executor)
    {
    }


    /**
     * @see EventManager
     */
    @Override
    public void setConfig(String executor)
    {
    }


    /**
     * @see EventManager
     */
    @Override
    public void onConfigAdd(String executor, ConfigSetting addedConfigSetting)
    {
    }


    /**
     * @see EventManager
     */
    @Override
    public void onConfigUpdate(String executor, ConfigSetting updateConfigSetting)
    {
    }


    /**
     * @see EventManager
     */
    @Override
    public void onConfigRemove(String executor, ConfigSetting removedConfigSetting)
    {
    }


    /**
     * @see EventManager
     */
    @Override
    public void getMarketData(String executor)
    {
    }


    /**
     * @see EventManager
     */
    @Override
    public void setMarketData(String executor)
    {
    }


    /**
     * @see EventManager
     */
    @Override
    public void onMarketDataUpdate(String producer, MarketDataSupplier supplier, MarketDataSource source, MarketDataType type, String id, byte[] marketDataUpdates, boolean isRetransmit) throws Exception
    {
        if(producer.equals(_producer) && supplier == MarketDataSupplier.ADEPT && source == MarketDataSource.ADEPT && type == MarketDataType.VALUATION_ENVIRONMENT && id.equals(_valuationEnvironmentMarketDataId))
        {
            MarketDataInputByteBuffer.parseMarketDataUpdatesByteArray(marketDataUpdates, _atTimeMarketDataUpdateCallback);
        }
    }


    /**
     * @see EventManager
     */
    @Override
    public void process(String executor)
    {
    }


    /**
     * @see EventManager
     */
    @Override
    public boolean hasChanged()
    {
        return false;
    }


    /**
     * @see EventManager
     */
    @Override
    public boolean isInitialized()
    {
        return false;
    }


    /**
     * @see EventManager
     */
    @Override
    public String getExecutor()
    {
        return null;
    }
}
