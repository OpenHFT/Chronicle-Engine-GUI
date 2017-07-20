package queue4;

import queue4.externalizableObjects.*;


/**
 * Should be implemented by all classes that handle events.
 * <p>
 * Please note that not all of the methods defined in this will be of relevance to all of the classes that implement the interface, hence, expect empty implementations.
 */
public interface EventManager extends EventManagerBase {
    /***
     * Called to startup an event manager.
     *
     * @param executor The executor that the event is of relevance to.
     */
    default void startup(String executor) {
    }


    /**
     * Called to stop an event manager.
     *
     * @param executor The executor that the event is of relevance to.
     */
    default void stopped(String executor) {
    }


    /**
     * @param executor The executor that the event is of relevance to.
     */
    void getConfig(String executor);


    /**
     * Called to signal that the configuration requested has all been updated.
     *
     * @param executor The executor that the event is of relevance to.
     */
    void setConfig(String executor);


    /**
     * Called when a configuration setting has been added.
     *
     * @param executor           The executor that the event is of relevance to.
     * @param addedConfigSetting The config setting that has been added.
     */
    void onConfigAdd(String executor, ConfigSetting addedConfigSetting);


    /**
     * Called when a configuration setting has been updated.
     *
     * @param executor            The executor that the event is of relevance to.
     * @param updateConfigSetting The config setting that has been updated.
     */
    void onConfigUpdate(String executor, ConfigSetting updateConfigSetting);


    /**
     * Called when a configuration setting has been removed.
     *
     * @param executor             The executor that the event is of relevance to.
     * @param removedConfigSetting The config setting that has been removed.
     */
    void onConfigRemove(String executor, ConfigSetting removedConfigSetting);


    /**
     * Called to get market data.
     *
     * @param executor The executor that the event is of relevance to.
     */
    void getMarketData(String executor);


    /**
     * Called to signal that the market data requested has all been updated.
     *
     * @param executor The executor that the event is of relevance to.
     */
    void setMarketData(String executor);


    /**
     * Called when a market data update has been received.
     *
     * @param producer          The producer of the data.
     * @param supplier          The supplier for the data.
     * @param source            The source of the data.
     * @param type              The type of the data.
     * @param id                The ID for the data.
     * @param marketDataUpdates The updates received.
     * @param isRetransmit      If this was a retransmit of data already published.
     */
    void onMarketDataUpdate(String producer, MarketDataSupplier supplier, MarketDataSource source, MarketDataType type, String id, byte[] marketDataUpdates, boolean isRetransmit) throws Exception;


    /**
     * Called to process the events consumed.
     *
     * @param executor The executor that the event is of relevance to.
     */
    void process(String executor);


    /**
     * @return true if this has changed since the last time that process was called.
     */
    boolean hasChanged();


    /**
     * Flag indicating whether the event manager has been initialized or not.
     *
     * @return boolean flag.  true if initialized, else false
     */
    boolean isInitialized();


    /**
     * Allows messages of predefined types to be sent to Chronicle.
     * <p>
     * Default behaviour is to do nothing.
     *
     * @param executor    The executor that the event is of relevance to.
     * @param messageType The message type
     * @param message     The message
     */
    default void setMessage(String executor, MessageType messageType, String message) {
    }

}
