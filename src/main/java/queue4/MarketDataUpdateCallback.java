package queue4;

import queue4.externalizableObjects.MarketDataField;

/**
 * Should be implemented by all classes that want to be notified when a market data update has been made.
 */
public interface MarketDataUpdateCallback {
    /**
     * Called when a boolean value has been updated.
     *
     * @param marketDataField The field that has been updated.
     * @param value           The value.
     */
    void onBooleanUpdate(MarketDataField marketDataField, boolean value);


    /**
     * Called when a char value has been updated.
     *
     * @param marketDataField The field that has been updated.
     * @param value           The value.
     */
    void onCharacterUpdate(MarketDataField marketDataField, char value);


    /**
     * Called when a int value has been updated.
     *
     * @param marketDataField The field that has been updated.
     * @param value           The value.r
     */
    void onIntegerUpdate(MarketDataField marketDataField, int value);


    /**
     * Called when a long value has been updated.
     *
     * @param marketDataField The field that has been updated.
     * @param value           The value.
     */
    void onLongUpdate(MarketDataField marketDataField, long value);


    /**
     * Called when a float value has been updated.
     *
     * @param marketDataField The field that has been updated.
     * @param value           The value.
     */
    void onFloatUpdate(MarketDataField marketDataField, float value);


    /**
     * Called when a double value has been updated.
     *
     * @param marketDataField The field that has been updated.
     * @param value           The value.
     */
    void onDoubleUpdate(MarketDataField marketDataField, double value);


    /**
     * Called when a String value has been updated.
     *
     * @param marketDataField The field that has been updated.
     * @param value           The value.
     */
    void onStringUpdate(MarketDataField marketDataField, String value);


    /**
     * Called when an object (non-primitive type, including String) has been updated.
     *
     * @param marketDataField The field that has been updated.
     * @param value           The value.
     */
    void onObjectUpdate(MarketDataField marketDataField, Object value);


    /**
     * Called when a byte[] has been updated.
     *
     * @param marketDataField The field that has been updated.
     * @param value           The value.
     */
    void onByteArrayUpdate(MarketDataField marketDataField, byte[] value);
}