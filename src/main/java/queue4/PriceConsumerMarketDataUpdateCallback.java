package queue4;

import queue4.externalizableObjects.MarketDataField;

/**
 * Should be extended by all those classes representing callbacks to be utilised by price consumers.
 */
public class PriceConsumerMarketDataUpdateCallback extends MarketDataUpdateCallbackBase {
    private boolean _isRetransmit;


    /**
     * @return true if the update is for a retransmit.
     */
    public boolean isRetransmit() {
        return _isRetransmit;
    }


    /**
     * Used to specify if the next update is for a retransmit (as this will be reused for more than one update).
     *
     * @param isRetransmit If the update is for a retransmit.
     */
    public void setRetransmit(boolean isRetransmit) {
        _isRetransmit = isRetransmit;
    }


    /**
     * Provides a no-op implementation.
     *
     * @see MarketDataUpdateCallback
     */
    public void onBooleanUpdate(MarketDataField marketDataField, boolean value) {
    }


    /**
     * Provides a no-op implementation.
     *
     * @see MarketDataUpdateCallback
     */
    public void onCharacterUpdate(MarketDataField marketDataField, char value) {
    }


    /**
     * Provides a no-op implementation.
     *
     * @see MarketDataUpdateCallback
     */
    public void onIntegerUpdate(MarketDataField marketDataField, int value) {
    }


    /**
     * Provides a no-op implementation.
     *
     * @see MarketDataUpdateCallback
     */
    public void onLongUpdate(MarketDataField marketDataField, long value) {
    }


    /**
     * Provides a no-op implementation.
     *
     * @see MarketDataUpdateCallback
     */
    public void onFloatUpdate(MarketDataField marketDataField, float value) {
    }


    /**
     * Provides a no-op implementation.
     *
     * @see MarketDataUpdateCallback
     */
    public void onDoubleUpdate(MarketDataField marketDataField, double value) {
    }


    /**
     * Provides a no-op implementation.
     *
     * @see MarketDataUpdateCallback
     */
    public void onStringUpdate(MarketDataField marketDataField, String value) {
    }


    /**
     * Provides a no-op implementation.
     *
     * @see MarketDataUpdateCallback
     */
    public void onObjectUpdate(MarketDataField marketDataField, Object value) {
    }


    /**
     * Provides a no-op implementation.
     *
     * @see MarketDataUpdateCallback
     */
    public void onByteArrayUpdate(MarketDataField marketDataField, byte[] value) {
    }


    /**
     * Called immediately before a new market update is to be processed.
     */
    public void startingToProcessNewMarketDataUpdate() {
    }


    /**
     * Called immediately after a new market update has been processed.
     */
    public void finishedProcessingNewMarketDataUpdate() {
    }
}
