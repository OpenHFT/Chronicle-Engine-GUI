package queue4;


import queue4.externalizableObjects.*;

/**
 * Should be extended by all classes that deals with market data updates.
 */
public abstract class MarketDataUpdateCallbackBase implements MarketDataUpdateCallback {
    protected String _marketDataProducer;
    protected MarketDataSupplier _marketDataSupplier;
    protected MarketDataSource _marketDataSource;
    protected MarketDataType _marketDataType;
    protected String _marketDataId;
    protected MarketDataKey _marketDataKey;
    protected boolean _isRetransmit;


    /**
     * Sets the producer to use.
     *
     * @param marketDataProducer The producer to use.
     */
    public void setMarketDataProducer(String marketDataProducer) {
        _marketDataProducer = marketDataProducer;
    }


    /**
     * Sets the supplier to use.
     *
     * @param marketDataSupplier The supplier to use.
     */
    public void setMarketDataSupplier(MarketDataSupplier marketDataSupplier) {
        _marketDataSupplier = marketDataSupplier;
    }


    /**
     * Sets the source to use.
     *
     * @param marketDataSource The source to use.
     */
    public void setMarketDataSource(MarketDataSource marketDataSource) {
        _marketDataSource = marketDataSource;
    }


    /**
     * Sets the type to use.
     *
     * @param marketDataType The type to use.
     */
    public void setMarketDataType(MarketDataType marketDataType) {
        _marketDataType = marketDataType;
    }


    /**
     * Sets the ID to use.
     *
     * @param marketDataId The ID to use.
     */
    public void setMarketDataId(String marketDataId) {
        _marketDataId = marketDataId;
    }


    /**
     * Returns a key representing a specified set of data, which allows a previously created value to be reused, which is more efficient.
     *
     * @param supplier The producer to use.
     * @param supplier The supplier to use.
     * @param source   The source to use.
     * @param type     The type to use.
     * @param id       The ID to use.
     * @param field    The field to use.
     * @return The key to use.
     */
    protected MarketDataKey getUpdatedTempKey(String producer, MarketDataSupplier supplier, MarketDataSource source, MarketDataType type, String id, MarketDataField field) {
        if (_marketDataKey == null) {
            _marketDataKey = new MarketDataKey(producer, supplier, source, type, id, field);
        } else {
            _marketDataKey.updateValues(producer, supplier, source, type, id, field);
        }

        return _marketDataKey;
    }


    /**
     * Used to specify if the next update is for a retransmit (as this will be reused for more than one update).
     *
     * @param isRetransmit If the update is for a retransmit.
     */
    public void setRetransmit(boolean isRetransmit) {
        _isRetransmit = isRetransmit;
    }
}